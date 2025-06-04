package me.jason.clanspvp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.jason.clanspvp.commands.ClanCommand;
import me.jason.clanspvp.commands.ClanTabCompleter;
import me.jason.clanspvp.commands.ClaimCommand;
import me.jason.clanspvp.listeners.PlayerDeathListener;
import me.jason.clanspvp.listeners.PlayerKillListener;
import me.jason.clanspvp.listeners.PlayerJoinListener;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ClansPvP extends JavaPlugin {

    private static ClansPvP instance;

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private ClanManager clanManager;
    private ClaimManager claimManager;

    private static Permission permissions = null;

    public static ClansPvP getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Initialize managers
        clanManager = new ClanManager();
        claimManager = new ClaimManager();

        // Setup Vault permissions
        setupPermissions();

        // Register only main /clan command
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("clan").setTabCompleter(new ClanTabCompleter());

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        getLogger().info("ClansPvP enabled.");

        // === Load Clans ===
        File clansFile = new File(getDataFolder(), "clans.yml");
        if (!clansFile.exists()) {
            try {
                clansFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);

        if (clansConfig.contains("clans")) {
            for (String clanName : clansConfig.getConfigurationSection("clans").getKeys(false)) {
                String tag = clansConfig.getString("clans." + clanName + ".tag");
                String leaderStr = clansConfig.getString("clans." + clanName + ".leader");
                UUID leader = leaderStr != null ? UUID.fromString(leaderStr) : null;
                Clan clan = new Clan(clanName, tag, leader);

                if (clansConfig.contains("clans." + clanName + ".members")) {
                    for (String memberStr : clansConfig.getConfigurationSection("clans." + clanName + ".members")
                            .getKeys(false)) {
                        UUID member = UUID.fromString(memberStr);
                        String role = clansConfig.getString("clans." + clanName + ".members." + memberStr + ".role");
                        clan.addMember(member, role);
                    }
                }

                clanManager.registerClan(clan);
            }
        }

        // === Load PlayerData ===
        File playerFile = new File(getDataFolder(), "playerdata.yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        if (playerConfig.contains("players")) {
            for (String uuidStr : playerConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerData data = new PlayerData(uuid);
                data.setKills(playerConfig.getDouble("players." + uuidStr + ".kills", 0));
                data.setDeaths(playerConfig.getDouble("players." + uuidStr + ".deaths", 0));
                data.setPower(playerConfig.getDouble("players." + uuidStr + ".power", 5.0));
                String clanName = playerConfig.getString("players." + uuidStr + ".clan", null);
                String clanRole = playerConfig.getString("players." + uuidStr + ".clanRole", null);

                if (clanName != null && clanManager.clanExists(clanName)) {
                    Clan clan = clanManager.getClan(clanName);
                    data.setClan(clan);
                    data.setClanRole(clanRole);
                }

                playerDataMap.put(uuid, data);
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ClansPvP disabled.");

        // === Save Clans ===
        File clansFile = new File(getDataFolder(), "clans.yml");
        FileConfiguration clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        clansConfig.set("clans", null);

        for (Clan clan : clanManager.getAllClans()) {
            String path = "clans." + clan.getName();
            clansConfig.set(path + ".tag", clan.getTag());
            clansConfig.set(path + ".leader", clan.getLeader().toString());

            for (UUID member : clan.getMembers().keySet()) {
                String role = clan.getMembers().get(member);
                clansConfig.set(path + ".members." + member.toString() + ".role", role);
            }
        }

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // === Save PlayerData ===
        File playerFile = new File(getDataFolder(), "playerdata.yml");
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerConfig.set("players", null);

        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();
            String path = "players." + uuid.toString();
            playerConfig.set(path + ".kills", data.getKills());
            playerConfig.set(path + ".deaths", data.getDeaths());
            playerConfig.set(path + ".power", data.getPower());
            playerConfig.set(path + ".clan", data.getClan() != null ? data.getClan().getName() : null);
            playerConfig.set(path + ".clanRole", data.getClanRole());
        }

        try {
            playerConfig.save(playerFile);
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
