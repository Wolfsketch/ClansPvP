package me.jason.clanspvp;

import com.google.gson.*;
import java.io.*;
import java.util.*;

import me.jason.clanspvp.commands.ClanCommand;
import me.jason.clanspvp.commands.ClanMapCommand;
import me.jason.clanspvp.commands.ClanTabCompleter;
import me.jason.clanspvp.commands.ClaimCommand;
import me.jason.clanspvp.listeners.ClaimVisualFeedbackListener;
import me.jason.clanspvp.listeners.PlayerDeathListener;
import me.jason.clanspvp.listeners.PlayerKillListener;
import me.jason.clanspvp.listeners.PlayerJoinListener;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ClansPvP extends JavaPlugin {

    private static ClansPvP instance;

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private ClanManager clanManager;
    private ClaimManager claimManager;

    private static Permission permissions = null;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static ClansPvP getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        clanManager = new ClanManager();
        claimManager = new ClaimManager();

        setupPermissions();

        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("clan").setTabCompleter(new ClanTabCompleter());
        getCommand("clanmap").setExecutor(new ClanMapCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new ClaimVisualFeedbackListener(this), this);

        getLogger().info("ClansPvP enabled.");

        loadClans();
        loadPlayerData();
        loadClaims();
    }

    @Override
    public void onDisable() {
        getLogger().info("ClansPvP disabled.");
        saveClans();
        savePlayerData();
        saveClaims();
    }

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
                JsonObject members = c.getAsJsonObject("members");
                for (String uuidStr : members.keySet()) {
                    String role = members.getAsJsonObject(uuidStr).get("role").getAsString();
                    clan.addMember(UUID.fromString(uuidStr), role);
                }
                clanManager.registerClan(clan);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveClans() {
        JsonObject root = new JsonObject();
        JsonObject clansJson = new JsonObject();
        for (Clan clan : clanManager.getAllClans()) {
            JsonObject c = new JsonObject();
            c.addProperty("tag", clan.getTag());
            c.addProperty("leader", clan.getLeader().toString());
            JsonObject members = new JsonObject();
            for (UUID uuid : clan.getMembers().keySet()) {
                JsonObject m = new JsonObject();
                m.addProperty("role", clan.getMembers().get(uuid));
                members.add(uuid.toString(), m);
            }
            c.add("members", members);
            clansJson.add(clan.getName(), c);
        }
        root.add("clans", clansJson);
        try (Writer writer = new FileWriter(new File(getDataFolder(), "clans.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    private void savePlayerData() {
        JsonObject root = new JsonObject();
        JsonObject players = new JsonObject();
        for (UUID uuid : playerDataMap.keySet()) {
            PlayerData pd = playerDataMap.get(uuid);
            JsonObject obj = new JsonObject();
            obj.addProperty("kills", pd.getKills());
            obj.addProperty("deaths", pd.getDeaths());
            obj.addProperty("power", pd.getPower());
            obj.addProperty("clan", pd.getClan() != null ? pd.getClan().getName() : null);
            obj.addProperty("clanRole", pd.getClanRole());
            players.add(uuid.toString(), obj);
        }
        root.add("players", players);
        try (Writer writer = new FileWriter(new File(getDataFolder(), "playerdata.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveClaims() {
        JsonObject root = new JsonObject();
        for (Chunk chunk : claimManager.getAllClaims().keySet()) {
            String key = claimManager.getClaimKey(chunk);
            root.addProperty(key, claimManager.getClaimOwner(chunk));
        }
        try (Writer writer = new FileWriter(new File(getDataFolder(), "claims.json"))) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public String getPermissionGroup(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && permissions != null) {
            String[] groups = permissions.getPlayerGroups(player);
            return (groups.length > 0) ? groups[0] : "default";
        }
        return "default";
    }

    private void setupPermissions() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager()
                    .getRegistration(Permission.class);
            if (rsp != null) {
                permissions = rsp.getProvider();
                getLogger().info("Vault permissions successfully linked.");
            } else {
                getLogger().warning("Vault permissions provider not found.");
            }
        } else {
            getLogger().warning("Vault plugin not found!");
        }
    }
}