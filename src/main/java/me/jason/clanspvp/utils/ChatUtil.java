package me.jason.clanspvp.utils;

import org.bukkit.ChatColor;

public class ChatUtil {

    public static String color(String message) {
        if (message == null) {
            return ChatColor.translateAlternateColorCodes('&', "&c✘ &7[Invalid message]");
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
