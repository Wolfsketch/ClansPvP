package me.jason.clanspvp.utils;

import org.bukkit.ChatColor;

public class ChatUtil {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
