package me.jason.clanspvp.utils;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ClanScoreboard {

    public static void showClanScoreboard(Player player, Clan clan, PlayerData data) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("claninfo", "dummy", ChatColor.GOLD + "✦ Clan Info ✦");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        ClaimManager claimManager = ClansPvP.getInstance().getClaimManager();
        ClanManager clanManager = ClansPvP.getInstance().getClanManager();

        int claimCount = claimManager.getClaimCount(clan.getName());
        int maxClaims = ConfigManager.get().getInt("claiming.max-chunks-per-clan-member")
                * clanManager.getMembers(clan).size();

        // BELANGRIJK: Scores lopen van hoog naar laag!
        obj.getScore(ChatColor.GRAY + " ").setScore(10);
        obj.getScore(ChatColor.YELLOW + "Name: " + ChatColor.GREEN + clan.getName()).setScore(9);
        obj.getScore(ChatColor.YELLOW + "Tag: " + ChatColor.AQUA + "[" + clan.getTag() + "]").setScore(8);
        obj.getScore(
                ChatColor.YELLOW + "Leader: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(clan.getLeader()).getName())
                .setScore(7);
        obj.getScore(ChatColor.YELLOW + "Power: " + ChatColor.RED + clan.getPower() + ChatColor.GRAY + " / "
                + ChatColor.GREEN + clan.getMaxPower()).setScore(6);
        obj.getScore(ChatColor.YELLOW + "Your Power: " + ChatColor.GOLD + data.getPower()).setScore(5);
        obj.getScore(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + clan.getMembers().size()).setScore(4);
        obj.getScore(ChatColor.YELLOW + "Claims: " + ChatColor.AQUA + claimCount + ChatColor.GRAY + " / "
                + ChatColor.DARK_AQUA + maxClaims).setScore(3);

        // Optioneel: ranks tonen
        obj.getScore(
                ChatColor.YELLOW + "Leader: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(clan.getLeader()).getName())
                .setScore(2);

        // Reset tussenruimte
        obj.getScore(ChatColor.GRAY + "  ").setScore(1);

        player.setScoreboard(board);
    }

    public static void showNoClanScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("noclan", "dummy", ChatColor.GOLD + "✦ Clan Info ✦");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(ChatColor.GRAY + " ").setScore(2);
        obj.getScore(ChatColor.RED + "No clan joined!").setScore(1);

        player.setScoreboard(board);
    }
}
