package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ChatUtil;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.utils.ClanScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor {

    private final ClansPvP plugin;

    public ClanCommand() {
        this.plugin = ClansPvP.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use clan commands.");
            return true;
        }

        Player player = (Player) sender;
        ClanManager clanManager = plugin.getClanManager();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        String line = ChatUtil.color("§8§m                                                           ");

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&6                         ClansPvP Help"));
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&e➤ &7/clan create &8<&fname&8> [&ftag&8] &8– &fCreate a new clan"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan info &8– &fView clan information"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan vault &8– &fOpen your clan vault"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan invite &8<&fplayer&8> &8– &fInvite a player"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan leave &8– &fLeave your current clan"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan promote &8<&fplayer&8> &8– &fPromote a member"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan demote &8<&fplayer&8> &8– &fDemote a member"));
            player.sendMessage(ChatUtil.color("&e➤ &7/clan raid start | stop | check &8– &fManage raids"));
            player.sendMessage(" ");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (data.getClan() == null) {
                // Toon scoreboard voor geen clan
                ClanScoreboard.showNoClanScoreboard(player);
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            Clan clan = data.getClan();
            // Toon scoreboard met claninfo
            ClanScoreboard.showClanScoreboard(player, clan, data);
            player.sendMessage(ChatUtil.color("&aClan info shown on your scoreboard!"));

            // Geavanceerde Info:
            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&6                         ✦ Clan Info ✦"));
            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&7Name:      &e" + clan.getName()));
            player.sendMessage(ChatUtil.color("&7Tag:       &a[" + clan.getTag() + "]"));
            player.sendMessage(ChatUtil.color("&7Leader:    &b" + Bukkit.getOfflinePlayer(clan.getLeader()).getName()));

            // Clan power & jouw power (pas deze code aan als je power anders opslaat!)
            double clanPower = clan.getPower();
            double maxPower = clan.getMaxPower();
            double playerPower = data.getPower();

            player.sendMessage(ChatUtil.color("&7Power:     &c" + clanPower + "&7 / &a" + maxPower));
            player.sendMessage(ChatUtil.color("&7Your Power: &e" + playerPower));
            player.sendMessage(" ");

            // Toon leden per rank
            player.sendMessage(ChatUtil.color("&6Members:"));

            sendMembersByRole(clan, "LEADER", "&eLeader:   ", player);
            sendMembersByRole(clan, "OFFICER", "&eOfficers: ", player);
            sendMembersByRole(clan, "MEMBER", "&eMembers:  ", player);
            sendMembersByRole(clan, "RECRUIT", "&eRecruits: ", player);
            player.sendMessage(line);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!ConfigManager.get().getBoolean("clan-creation.enabled")) {
                player.sendMessage(ChatUtil.color("&c✗ Clan creation is currently disabled."));
                return true;
            }

            if (ConfigManager.get().getBoolean("clan-creation.require-permission") &&
                    !player.hasPermission("clanspvp.create")) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("no-permission")));
                return true;
            }

            if (data.getClan() != null) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("player-already-in-clan")));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan create <name> [tag]"));
                return true;
            }

            String name = args[1];
            String tag = args.length >= 3 ? args[2] : name.substring(0, Math.min(name.length(), 3)).toUpperCase();

            if (clanManager.clanExists(name)) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-exists")));
                return true;
            }

            int maxTagLength = ConfigManager.get().getInt("clan-creation.max-tag-length");
            if (tag.length() > maxTagLength) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-tag-too-long")
                        .replace("%max%", String.valueOf(maxTagLength))));
                return true;
            }

            double requiredKDR = ConfigManager.get().getDouble("clan-creation.min-kdr-to-create");
            if (data.getKDR() < requiredKDR) {
                player.sendMessage(ChatUtil.color(ConfigManager.getMessage("not-enough-kdr")
                        .replace("%kdr", String.valueOf(requiredKDR))));
                return true;
            }

            Clan clan = new Clan(name, tag, player.getUniqueId());
            clanManager.registerClan(clan);
            data.setClan(clan);
            data.setClanRole("LEADER");

            String msg = ConfigManager.getMessage("clan-created")
                    .replace("%name%", name)
                    .replace("%tag%", tag);
            player.sendMessage(ChatUtil.color(msg));
            return true;
        }

        player.sendMessage(ChatUtil.color("&c✗ Unknown subcommand. Use &7/clan help"));
        return true;
    }

    // Helper om leden per rank weer te geven
    private void sendMembersByRole(Clan clan, String role, String prefix, Player toSend) {
        List<UUID> members = clan.getMembersByRole(role); // zorg dat deze bestaat en een lijst UUIDs teruggeeft!
        String names = members.isEmpty() ? ChatColor.GRAY + "None"
                : members.stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .collect(Collectors.joining(", "));

        toSend.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + names));
    }
}
