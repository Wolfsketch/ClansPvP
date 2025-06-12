package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.managers.ClaimManager;
import me.jason.clanspvp.managers.ConfigManager;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Iterator;
import java.util.Random;

/**
 * Allows TNT to break blocks inside claimed land, including Obsidian with a
 * two-shot mechanic (first hit "cracks", second hit breaks).
 *
 * • Config section:
 * raiding:
 * tnt-breaks-obsidian: true
 * obsidian-break-chance: 0.5 # 50 % chance to crack on first hit
 *
 * • Ally rules:
 * - If the defending clan has friendly-fire disabled, allied TNT will NOT crack
 * Obsidian.
 * - Enemy or neutral TNT always works.
 */
public class TNTExplosionListener implements Listener {

    private final ClansPvP plugin = ClansPvP.getInstance();
    private final Random rng = new Random();

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {

        /* ---------- quick opt-out ---------- */
        if (!ConfigManager.get().getBoolean("raiding.tnt-breaks-obsidian", true))
            return;

        ClaimManager claimManager = plugin.getClaimManager();
        double crackChance = ConfigManager.get()
                .getDouble("raiding.obsidian-break-chance", 0.5);

        /* ---------- identify attacking clan (if a player primed the TNT) ---------- */
        Entity source = event.getEntity();
        Clan attackerClan = null;
        if (source instanceof Player p) {
            PlayerData d = plugin.getPlayerData(p.getUniqueId());
            attackerClan = d.getClan();
        }

        /* ---------- iterate through affected blocks ---------- */
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();

            Chunk chunk = block.getChunk();
            String ownerName = claimManager.getClaimOwner(chunk);
            if (ownerName == null)
                continue; // Wilderness

            Clan ownerClan = plugin.getClanManager().getClan(ownerName);

            /*
             * ALLOW always if NOT allied or FF enabled.
             * CANCEL Obsidian damage if allied & FF disabled.
             */
            boolean allyButProtected = attackerClan != null &&
                    attackerClan.isAlliedWith(ownerName) &&
                    !ownerClan.isAllowFriendlyFire();

            /* ---------- Obsidian logic ---------- */
            if (block.getType() == Material.OBSIDIAN) {

                // protected allies? keep Obsidian intact
                if (allyButProtected) {
                    it.remove();
                    continue;
                }

                // two-shot system with chance on first hit
                if (!block.hasMetadata("clanspvp_cracked")) {
                    // FIRST HIT
                    if (rng.nextDouble() <= crackChance) {
                        block.setMetadata("clanspvp_cracked",
                                new FixedMetadataValue(plugin, System.currentTimeMillis()));
                        // Optional: visual cue
                        // block.setType(Material.CRYING_OBSIDIAN);
                    }
                    it.remove(); // keep block for now
                } else {
                    // SECOND HIT – break for good
                    block.removeMetadata("clanspvp_cracked", plugin);
                    // block remains in list → gets destroyed by vanilla blast
                }
            }

            /*
             * ---------- non-Obsidian blocks ----------
             * We leave them in the list so vanilla explosion breaks them.
             */
        }
    }
}
