package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RaidCommand implements CommandExecutor {

    private final ClansPvP plugin = ClansPvP.getInstance();
    private final Map<String, Location> activeRaids = new HashMap<>(); // clanName → Location

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtil.color("&cOnly players can use raid commands."));
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        Clan clan = data.getClan();

        if (clan == null) {
            player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
            return true;
        }

        String clanName = clan.getName().toLowerCase();

        if (args.length == 0) {
            player.sendMessage(ChatUtil.color("&eUse &f/clan raid <start|check|stop>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start":
                if (!data.getClanRole().equalsIgnoreCase("LEADER")) {
                    player.sendMessage(ChatUtil.color("&c✗ Only the clan leader can start a raid."));
                    return true;
                }
                Location loc = player.getLocation();
                activeRaids.put(clanName, loc);
                player.sendMessage(ChatUtil.color("&a✓ Raid started at &fX: " + loc.getBlockX() +
                        ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ()));
                break;

            case "check":
                if (activeRaids.containsKey(clanName)) {
                    Location l = activeRaids.get(clanName);
                    player.sendMessage(ChatUtil.color("&b📍 Current raid location: &fX: " + l.getBlockX() +
                            ", Y: " + l.getBlockY() + ", Z: " + l.getBlockZ()));
                } else {
                    player.sendMessage(ChatUtil.color("&c✗ No raid is currently active."));
                }
                break;

            case "stop":
                if (!data.getClanRole().equalsIgnoreCase("LEADER")) {
                    player.sendMessage(ChatUtil.color("&c✗ Only the clan leader can stop a raid."));
                    return true;
                }
                if (activeRaids.remove(clanName) != null) {
                    player.sendMessage(ChatUtil.color("&a✓ Raid has been stopped."));
                } else {
                    player.sendMessage(ChatUtil.color("&c✗ No raid was active."));
                }
                break;

            default:
                player.sendMessage(ChatUtil.color("&c✗ Unknown subcommand. Use &f/clan raid <start|check|stop>"));
        }

        return true;
    }
}
