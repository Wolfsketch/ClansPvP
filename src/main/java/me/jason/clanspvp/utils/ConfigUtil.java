package me.jason.clanspvp.utils;

import me.jason.clanspvp.ClansPvP;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUtil {

    private static final ClansPvP plugin = ClansPvP.getInstance();

    /**
     * Haalt een string op en verwerkt kleuren (color codes).
     */
    public static String getColored(String path) {
        return ChatUtil.color(plugin.getConfig().getString(path, "&c✗ Message not found: " + path));
    }

    /**
     * Haalt een integer op uit config.
     */
    public static int getInt(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    /**
     * Haalt een boolean op uit config.
     */
    public static boolean getBoolean(String path) {
        return plugin.getConfig().getBoolean(path, false);
    }

    /**
     * Herlaadt de config.yml
     */
    public static void reload() {
        plugin.reloadConfig();
    }

    /**
     * Config opslaan.
     */
    public static void save() {
        plugin.saveConfig();
    }

    /**
     * Config ophalen
     */
    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
