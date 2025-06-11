package me.jason.clanspvp.managers;

import me.jason.clanspvp.ClansPvP;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static final ClansPvP plugin = ClansPvP.getInstance();

    public static FileConfiguration get() {
        return plugin.getConfig();
    }

    public static String getMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key);
        if (msg == null) {
            plugin.getLogger().warning("[ClansPvP] Missing message key: " + key);
            return "&c✘ &7[Missing message: " + key + "]";
        }
        return msg;
    }

    public static void reload() {
        plugin.reloadConfig();
    }
}
