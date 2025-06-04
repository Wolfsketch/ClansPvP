package me.jason.clanspvp.commands;

import me.jason.clanspvp.managers.ClanManager;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.Clan;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final ClaimManager claimManager;

    public ClaimCommand(ClanManager clanManager, ClaimManager claimManager) {
        this.clanManager = clanManager;
        this.claimManager = claimManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c✘ Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Get the player's clan
        Clan clan = clanManager.getClanByMember(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§c✘ You are not part of any clan.");
            return true;
        }

        // Get current chunk
        Chunk chunk = player.getLocation().getChunk();

        // Check if the chunk is already claimed
        if (claimManager.isClaimed(chunk)) {
            player.sendMessage("§c✘ This chunk is already claimed.");
            return true;
        }

        // Check claim limit based on number of members
        String clanName = clan.getName();
        int currentClaims = claimManager.getClaimCount(clanName);
        int maxClaims = clanManager.getMembers(clan).size(); // Or fetch from config if needed

        if (currentClaims >= maxClaims) {
            player.sendMessage("§c✘ Your clan has reached the maximum number of claimed chunks.");
            return true;
        }

        // Try to claim the chunk
        boolean success = claimManager.claimChunk(clanName, chunk, maxClaims);
        if (!success) {
            player.sendMessage("§c✘ Failed to claim this land.");
            return true;
        }

        player.sendMessage("§a✔ You have successfully claimed this land for your clan.");
        return true;
    }
}
