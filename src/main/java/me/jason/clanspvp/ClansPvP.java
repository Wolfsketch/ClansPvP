package me.jason.clanspvp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.jason.clanspvp.commands.ClanCommand;
import me.jason.clanspvp.commands.ClanTabCompleter;
import me.jason.clanspvp.commands.ClaimCommand;
import me.jason.clanspvp.listeners.PlayerDeathListener;
import me.jason.clanspvp.listeners.PlayerKillListener;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.PlayerData;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
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

        // Managers
        clanManager = new ClanManager();
        claimManager = new ClaimManager();

        // Vault setup
        setupPermissions();

        // Command executors
        getCommand("clan").setExecutor(new ClanCommand());
        getCommand("clan").setTabCompleter(new ClanTabCompleter());
        getCommand("claim").setExecutor(new ClaimCommand());

        // Event listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(), this);

        getLogger().info("ClansPvP enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClansPvP disabled.");
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
