package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.managers.ClaimManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final ClansPvP plugin;

    public BlockBreakListener() {
        this.plugin = ClansPvP.getInstance();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        ClaimManager claimManager = plugin.getClaimManager();
        String claimedBy = claimManager.getClaimOwner(chunk);

        if (claimedBy == null)
            return; // Niet geclaimd → geen probleem

        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        if (data.getClan() == null || !data.getClan().getName().equalsIgnoreCase(claimedBy)) {
            // Speler is niet in de juiste clan
            event.setCancelled(true);
            player.sendMessage("§c✗ You cannot break blocks in territory claimed by another clan.");
        }
    }
}
