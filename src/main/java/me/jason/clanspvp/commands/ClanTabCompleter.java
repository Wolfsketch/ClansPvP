package me.jason.clanspvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClanTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "create", "info", "vault", "invite", "leave", "promote", "demote",
                    "claim", "unclaim", "disband", "setwarp", "warp", "raid");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("raid")) {
            return Arrays.asList("start", "stop", "check");
        }

        return Collections.emptyList();
    }
}
