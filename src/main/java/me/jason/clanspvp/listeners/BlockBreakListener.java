package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Cancels block-breaking in claimed chunks unless:
 * • the player’s own clan owns the chunk, OR
 * • the chunk’s owner clan is allied with the player’s clan AND
 * *both* clans have mutually enabled “trusted build” for each other.
 */
public class BlockBreakListener implements Listener {

    private final ClansPvP plugin;

    public BlockBreakListener() {
        this.plugin = ClansPvP.getInstance();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        ClaimManager claimMgr = plugin.getClaimManager();
        String ownerName = claimMgr.getClaimOwner(chunk); // null → not claimed

        if (ownerName == null) {
            return; // free land: allow break
        }

        // Retrieve player / clan data
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        Clan playerClan = playerData.getClan();

        // Player not in a clan → block
        if (playerClan == null) {
            deny(event, player);
            return;
        }

        // Own clan land → allow
        if (playerClan.getName().equalsIgnoreCase(ownerName)) {
            return;
        }

        // Otherwise, check ally & trust rules
        Clan ownerClan = plugin.getClanManager().getClan(ownerName);
        if (ownerClan != null
                && playerClan.isAlliedWith(ownerName)
                && playerClan.isAllyAllowedToBuild(ownerName) // my clan trusts them
                && ownerClan.isAllyAllowedToBuild(playerClan.getName())) { // they trust me
            return; // mutual trust: allow
        }

        // Everything else → block
        deny(event, player);
    }

    /* Utility: cancel & message */
    private void deny(BlockBreakEvent e, Player p) {
        e.setCancelled(true);
        p.sendMessage("§c✘ You cannot break blocks in land claimed by another clan.");
    }
}
