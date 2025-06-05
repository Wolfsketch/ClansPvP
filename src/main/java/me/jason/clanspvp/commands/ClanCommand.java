package me.jason.clanspvp.commands;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ChatUtil;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.utils.ClanScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor {

    private final ClansPvP plugin;
    private final Map<UUID, Long> disbandConfirmations = new ConcurrentHashMap<>();

    public ClanCommand(ClansPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use clan commands.");
            return true;
        }

        Player player = (Player) sender;
        ClanManager clanManager = plugin.getClanManager();
        ClaimManager claimManager = plugin.getClaimManager();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        String line = ChatUtil.color("\u00a78\u00a7m                                                           ");

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&6                         ClansPvP Help"));
            player.sendMessage(" ");
            player.sendMessage(ChatUtil.color("&e→ &7/clan create &8<&fname&8> [&ftag&8] &8– &fCreate a new clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan info &8– &fView clan information"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan vault &8– &fOpen your clan vault"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan invite &8<&fplayer&8> &8– &fInvite a player"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan leave &8– &fLeave your current clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan disband &8– &fDisband your clan (leader only)"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan confirm &8– &fConfirm disbanding your clan"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan promote &8<&fplayer&8> &8– &fPromote a member"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan demote &8<&fplayer&8> &8– &fDemote a member"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan claim &8– &fClaim the land you stand on"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan unclaim &8– &fUnclaim the land you stand on"));
            player.sendMessage(ChatUtil.color("&e→ &7/clan raid start | stop | check &8– &fManage raids"));
            player.sendMessage(" ");
            return true;
        }

        if (args[0].equalsIgnoreCase("claim")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            Chunk chunk = player.getLocation().getChunk();
            if (claimManager.isClaimed(chunk)) {
                player.sendMessage(ChatUtil.color("&c✗ This land is already claimed."));
                return true;
            }

            String clanName = clan.getName();
            int currentClaims = claimManager.getClaimCount(clanName);
            int maxClaims = ConfigManager.get().getInt("claiming.max-chunks-per-clan-member")
                    * clanManager.getMembers(clan).size();

            if (currentClaims >= maxClaims) {
                player.sendMessage(ChatUtil.color("&c✗ Your clan has reached the maximum number of claimed chunks."));
                return true;
            }

            boolean success = claimManager.claimChunk(clanName, chunk, maxClaims);
            if (success) {
                player.sendMessage(ChatUtil.color("&a✓ You have successfully claimed this land for your clan!"));
            } else {
                player.sendMessage(ChatUtil.color("&c✗ Failed to claim the chunk."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unclaim")) {
            Clan clan = data.getClan();
            if (clan == null) {
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            Chunk chunk = player.getLocation().getChunk();

            if (!claimManager.isClaimed(chunk)) {
                player.sendMessage(ChatUtil.color("&c✗ This chunk is not claimed."));
                return true;
            }

            String owner = claimManager.getClaimOwner(chunk);
            if (!clan.getName().equalsIgnoreCase(owner)) {
                player.sendMessage(ChatUtil.color("&c✗ You can only unclaim your own clan's territory."));
                return true;
            }

            boolean success = claimManager.unclaimChunk(chunk);
            if (success) {
                player.sendMessage(ChatUtil.color("&a✓ You have unclaimed this chunk."));
            } else {
                player.sendMessage(ChatUtil.color("&c✗ Failed to unclaim the chunk."));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (data.getClan() == null) {
                ClanScoreboard.showNoClanScoreboard(player);
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            Clan clan = data.getClan();
            ClanScoreboard.showClanScoreboard(player, clan, data);
            player.sendMessage(ChatUtil.color("&aClan info shown on your scoreboard!"));

            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&6                         ✦ Clan Info ✦"));
            player.sendMessage(line);
            player.sendMessage(ChatUtil.color("&7Name:      &e" + clan.getName()));
            player.sendMessage(ChatUtil.color("&7Tag:       &a[" + clan.getTag() + "]"));
            player.sendMessage(ChatUtil.color("&7Leader:    &b" + Bukkit.getOfflinePlayer(clan.getLeader()).getName()));

            double clanPower = clan.getPower();
            double maxPower = clan.getMaxPower();
            double playerPower = data.getPower();

            player.sendMessage(ChatUtil.color("&7Power:     &c" + clanPower + "&7 / &a" + maxPower));
            player.sendMessage(ChatUtil.color("&7Your Power: &e" + playerPower));

            int claimedChunks = claimManager.getClaimCount(clan.getName());
            int maxClaims = ConfigManager.get().getInt("claiming.max-chunks-per-clan-member")
                    * clanManager.getMembers(clan).size();
            player.sendMessage(ChatUtil.color("&7Claims:     &f" + claimedChunks + "&7 / &f" + maxClaims));

            player.sendMessage(" ");

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

            player.sendMessage(ChatUtil.color(ConfigManager.getMessage("clan-created")
                    .replace("%name%", name).replace("%tag%", tag)));
            ClanScoreboard.showClanScoreboard(player, clan, data);
            return true;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            if (data.getClanRole().equals("LEADER")) {
                player.sendMessage(ChatUtil.color("&c✗ As the leader, use &e/clan disband &cto disband your clan."));
                return true;
            }

            Clan clan = data.getClan();
            clan.removeMember(player.getUniqueId());
            data.setClan(null);
            data.setClanRole(null);
            player.sendMessage(ChatUtil.color("&aYou left your clan."));
            ClanScoreboard.showNoClanScoreboard(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("disband")) {
            if (data.getClan() == null || !"LEADER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color("&c✗ Only the clan leader can disband the clan."));
                return true;
            }

            disbandConfirmations.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatUtil.color(
                    "&a✓ Are you sure you want to disband the clan? &eType &6/clan confirm &ewithin 30 seconds."));

            new BukkitRunnable() {
                @Override
                public void run() {
                    disbandConfirmations.remove(player.getUniqueId());
                }
            }.runTaskLater(plugin, 20 * 30);

            return true;
        }

        if (args[0].equalsIgnoreCase("confirm")) {
            if (!disbandConfirmations.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatUtil.color("&c✗ You have no disband request to confirm."));
                return true;
            }

            disbandConfirmations.remove(player.getUniqueId());
            Clan clan = data.getClan();

            for (UUID member : clan.getAllMembers()) {
                PlayerData memberData = plugin.getPlayerData(member);
                memberData.setClan(null);
                memberData.setClanRole(null);
                Player online = Bukkit.getPlayer(member);
                if (online != null) {
                    ClanScoreboard.showNoClanScoreboard(online);
                    online.sendMessage(ChatUtil.color("&c✗ Your clan has been disbanded."));
                }
            }

            plugin.getClanManager().unregisterClan(clan.getName());
            player.sendMessage(ChatUtil.color("&a✓ Clan disbanded successfully."));
            return true;
        }

        if (args[0].equalsIgnoreCase("promote") || args[0].equalsIgnoreCase("demote")) {
            if (data.getClan() == null) {
                player.sendMessage(ChatUtil.color("&c✗ You are not in a clan."));
                return true;
            }

            Clan clan = data.getClan();

            if (!"LEADER".equals(data.getClanRole()) && !"OFFICER".equals(data.getClanRole())) {
                player.sendMessage(ChatUtil.color("&c✗ Only the leader or officers can promote or demote members."));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(ChatUtil.color("&c✗ Usage: /clan " + args[0] + " <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatUtil.color("&c✗ That player is not online."));
                return true;
            }

            PlayerData targetData = plugin.getPlayerData(target.getUniqueId());

            if (targetData.getClan() == null || !targetData.getClan().getName().equals(clan.getName())) {
                player.sendMessage(ChatUtil.color("&c✗ That player is not in your clan."));
                return true;
            }

            String currentRole = targetData.getClanRole();
            List<String> roles = Arrays.asList("RECRUIT", "MEMBER", "OFFICER", "LEADER");

            int currentIndex = roles.indexOf(currentRole);
            int newIndex = args[0].equalsIgnoreCase("promote") ? currentIndex + 1 : currentIndex - 1;

            // Blokkeer promotie naar LEADER (gebruik liever overdrachtsmechanisme)
            if (newIndex < 0 || newIndex >= roles.size() || "LEADER".equals(roles.get(newIndex))) {
                player.sendMessage(ChatUtil.color("&c✗ Cannot " + args[0] + " this player further."));
                return true;
            }

            // Verhindert dat de laatste leider zichzelf demote
            if (args[0].equalsIgnoreCase("demote") && currentRole.equals("LEADER")) {
                long leaderCount = clan.getMembers().entrySet().stream()
                        .filter(entry -> "LEADER".equals(entry.getValue()))
                        .count();

                if (leaderCount <= 1 && target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(ChatUtil.color("&c✗ You are the only leader. You cannot demote yourself."));
                    return true;
                }
            }

            targetData.setClanRole(roles.get(newIndex));
            target.sendMessage(ChatUtil.color("&a✓ You have been "
                    + (args[0].equalsIgnoreCase("promote") ? "promoted" : "demoted") + " to &e" + roles.get(newIndex)));
            player.sendMessage(ChatUtil.color("&a✓ " + target.getName() + " is now a(n) &e" + roles.get(newIndex)));

            return true;
        }

        player.sendMessage(ChatUtil.color("&c✗ Unknown subcommand. Use &7/clan help"));
        return true;
    }

    private void sendMembersByRole(Clan clan, String role, String prefix, Player toSend) {
        List<UUID> members = clan.getMembersByRole(role);
        String names = members.isEmpty() ? ChatColor.GRAY + "None"
                : members.stream()
                        .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                        .collect(Collectors.joining(", "));

        toSend.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + names));
    }
}
