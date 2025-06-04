package me.jason.clanspvp.managers;

import me.jason.clanspvp.ClansPvP;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static final ClansPvP plugin = ClansPvP.getInstance();

    public static FileConfiguration get() {
        return plugin.getConfig();
    }

    public static String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key);
    }
}
