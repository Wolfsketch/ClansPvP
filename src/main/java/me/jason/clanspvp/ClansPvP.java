package me.jason.clanspvp;

import com.google.gson.*;
import me.jason.clanspvp.commands.ClanCommand;
import me.jason.clanspvp.commands.ClanMapCommand;
import me.jason.clanspvp.commands.ClanTabCompleter;
import me.jason.clanspvp.listeners.*;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ClanScoreboard;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

/**
 * Main plugin bootstrap.
 */
public class ClansPvP extends JavaPlugin {

    /*
     * -------------------------------------------------------------
     * Singleton & managers
     * -----------------------------------------------------------
     */
    private static ClansPvP instance;

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private ClanManager clanManager;
    private ClaimManager claimManager;

    private static Permission permissions = null;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ClansPvP getInstance() {
        return instance;
    }

    /*
     * -------------------------------------------------------------
     * Plugin lifecycle
     * -----------------------------------------------------------
     */
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        clanManager = new ClanManager();
        claimManager = new ClaimManager();

        setupPermissions();

        /* Command / tab-complete */
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("clan").setTabCompleter(new ClanTabCompleter());
        getCommand("clanmap").setExecutor(new ClanMapCommand(this));

        /* Event listeners */
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimVisualFeedbackListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new TNTExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);

        getLogger().info("ClansPvP enabled.");

        loadClans();
        loadPlayerData();
        loadClaims();
    }

    @Override
    public void onDisable() {
        saveClans();
        savePlayerData();
        saveClaims();
        getLogger().info("ClansPvP disabled.");
    }

    /*
     * -------------------------------------------------------------
     * Clan persistence (allies + trusted build flags)
     * -----------------------------------------------------------
     */
    private void loadClans() {
        File file = new File(getDataFolder(), "clans.json");
        if (!file.exists())
            return;

        try (Reader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            JsonObject clansJson = root.getAsJsonObject("clans");

            for (String name : clansJson.keySet()) {
                JsonObject c = clansJson.getAsJsonObject(name);
                UUID leader = UUID.fromString(c.get("leader").getAsString());
                String tag = c.get("tag").getAsString();
                Clan clan = new Clan(name, tag, leader);

                /* members */
                JsonObject members = c.getAsJsonObject("members");
                for (String uuidStr : members.keySet()) {
                    String role = members.getAsJsonObject(uuidStr).get("role").getAsString();
                    clan.addMember(UUID.fromString(uuidStr), role);
                }

                /* allies */
                if (c.has("allies")) {
                    c.getAsJsonArray("allies").forEach(el -> clan.addAlly(el.getAsString()));
                }

                /* trusted allies for build/break */
                if (c.has("trustedAllies")) {
                    c.getAsJsonArray("trustedAllies")
                            .forEach(el -> clan.setAllyBuildTrust(el.getAsString(), true));
                }

                /* friendly-fire toggle */
                if (c.has("friendlyFire"))
                    clan.setAllowFriendlyFire(c.get("friendlyFire").getAsBoolean());

                clanManager.registerClan(clan);
            }
        } catch (IOException e) {
            getLogger().severe("Could not load clans.json");
            e.printStackTrace();
        }
    }

    public void saveClans() {
        JsonObject root = new JsonObject();
        JsonObject clansJson = new JsonObject();

        for (Clan clan : clanManager.getAllClans()) {
            JsonObject c = new JsonObject();
            c.addProperty("tag", clan.getTag());
            c.addProperty("leader", clan.getLeader().toString());

            /* members */
            JsonObject members = new JsonObject();
            for (UUID uuid : clan.getMembers().keySet()) {
                JsonObject m = new JsonObject();
                m.addProperty("role", clan.getMembers().get(uuid));
                members.add(uuid.toString(), m);
            }
            c.add("members", members);

            /* allies */
            JsonArray alliesArr = new JsonArray();
            clan.getAllies().forEach(alliesArr::add);
            c.add("allies", alliesArr);

            /* trusted allies */
            JsonArray trustedArr = new JsonArray();
            clan.getAllies().stream()
                    .filter(clan::isAllyAllowedToBuild)
                    .forEach(trustedArr::add);
            c.add("trustedAllies", trustedArr);

            /* friendly fire */
            c.addProperty("friendlyFire", clan.isAllowFriendlyFire());

            clansJson.add(clan.getName(), c);
        }
        root.add("clans", clansJson);

        try (Writer writer = new FileWriter(new File(getDataFolder(), "clans.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            getLogger().severe("Could not save clans.json");
            e.printStackTrace();
        }
    }

    /*
     * -------------------------------------------------------------
     * Player-data persistence
     * -----------------------------------------------------------
     */
    private void loadPlayerData() {
        File file = new File(getDataFolder(), "playerdata.json");
        if (!file.exists())
            return;

        try (Reader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            JsonObject players = root.getAsJsonObject("players");

            for (String uuidStr : players.keySet()) {
                UUID uuid = UUID.fromString(uuidStr);
                JsonObject data = players.getAsJsonObject(uuidStr);
                PlayerData pd = new PlayerData(uuid);
                pd.setKills(data.get("kills").getAsDouble());
                pd.setDeaths(data.get("deaths").getAsDouble());
                pd.setPower(data.get("power").getAsDouble());

                /* restore clan ref */
                if (data.has("clan") && !data.get("clan").isJsonNull()) {
                    String clanName = data.get("clan").getAsString();
                    String role = data.get("clanRole").getAsString();
                    if (clanManager.clanExists(clanName)) {
                        Clan clan = clanManager.getClan(clanName);
                        pd.setClan(clan);
                        pd.setClanRole(role);
                    }
                }
                playerDataMap.put(uuid, pd);
            }
        } catch (IOException e) {
            getLogger().severe("Could not load playerdata.json");
            e.printStackTrace();
        }
    }

    private void savePlayerData() {
        JsonObject root = new JsonObject();
        JsonObject players = new JsonObject();

        for (UUID uuid : playerDataMap.keySet()) {
            PlayerData pd = playerDataMap.get(uuid);
            JsonObject o = new JsonObject();
            o.addProperty("kills", pd.getKills());
            o.addProperty("deaths", pd.getDeaths());
            o.addProperty("power", pd.getPower());
            o.addProperty("clan", pd.getClan() != null ? pd.getClan().getName() : null);
            o.addProperty("clanRole", pd.getClanRole());
            players.add(uuid.toString(), o);
        }
        root.add("players", players);

        try (Writer writer = new FileWriter(new File(getDataFolder(), "playerdata.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            getLogger().severe("Could not save playerdata.json");
            e.printStackTrace();
        }
    }

    /*
     * -------------------------------------------------------------
     * Claim persistence
     * -----------------------------------------------------------
     */
    private void loadClaims() {
        File file = new File(getDataFolder(), "claims.json");
        if (!file.exists())
            return;

        try (Reader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            for (String key : root.keySet()) {
                String[] parts = key.split(";");
                if (parts.length != 3)
                    continue;

                String world = parts[0];
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);

                Chunk chunk = Bukkit.getWorld(world).getChunkAt(x, z);
                String clan = root.get(key).getAsString();
                claimManager.forceClaimChunk(clan, chunk);
            }
        } catch (IOException e) {
            getLogger().severe("Could not load claims.json");
            e.printStackTrace();
        }
    }

    private void saveClaims() {
        JsonObject root = new JsonObject();
        for (Chunk c : claimManager.getAllClaims().keySet()) {
            root.addProperty(claimManager.getClaimKey(c), claimManager.getClaimOwner(c));
        }
        try (Writer writer = new FileWriter(new File(getDataFolder(), "claims.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            getLogger().severe("Could not save claims.json");
            e.printStackTrace();
        }
    }

    /*
     * -------------------------------------------------------------
     * Convenience getters
     * -----------------------------------------------------------
     */
    public PlayerData getPlayerData(UUID id) {
        return playerDataMap.computeIfAbsent(id, PlayerData::new);
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public String getPermissionGroup(UUID id) {
        Player p = Bukkit.getPlayer(id);
        if (p != null && permissions != null) {
            String[] groups = permissions.getPlayerGroups(p);
            return groups.length > 0 ? groups[0] : "default";
        }
        return "default";
    }

    /*
     * -------------------------------------------------------------
     * Vault permissions setup
     * -----------------------------------------------------------
     */
    private void setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin not found!");
            return;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            permissions = rsp.getProvider();
            getLogger().info("Vault permissions successfully linked.");
        } else {
            getLogger().warning("Vault permissions provider not found.");
        }
    }

    /*
     * -------------------------------------------------------------
     * /clan reload helper
     * -----------------------------------------------------------
     */
    public void reload() {
        reloadConfig();
        ConfigManager.reload();

        Bukkit.getOnlinePlayers().forEach(p -> {
            PlayerData data = getPlayerData(p.getUniqueId());
            if (data.getClan() != null)
                ClanScoreboard.showClanScoreboard(p, data.getClan(), data);
            else
                ClanScoreboard.showNoClanScoreboard(p);
        });
    }
}
