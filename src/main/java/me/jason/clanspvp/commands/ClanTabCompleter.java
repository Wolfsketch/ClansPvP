package me.jason.clanspvp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ClanTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            return Arrays.asList(
                    "create", "info", "vault", "invite", "leave", "promote", "demote",
                    "claim", "unclaim", "disband", "setwarp", "warp", "raid",
                    "accept", "deny", "join", "cancel", "friendlyfire", "reload", "ally");
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();

            // Subcommand options for /clan raid
            if (sub.equals("raid")) {
                return Arrays.asList("start", "stop", "check");
            }

            // Subcommand options for /clan friendlyfire
            if (sub.equals("friendlyfire")) {
                return Arrays.asList("on", "off");
            }

            // Subcommand options for /clan ally
            if (sub.equals("ally")) {
                return Arrays.asList("accept", "deny"); // Or suggest clan names if preferred
            }

            // Suggest online player names
            if (Arrays.asList("invite", "promote", "demote", "accept", "deny").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
