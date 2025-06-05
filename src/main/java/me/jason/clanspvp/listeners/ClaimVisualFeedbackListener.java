package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.managers.ClaimManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimVisualFeedbackListener implements Listener {

    private final ClansPvP plugin;

    public ClaimVisualFeedbackListener(ClansPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();

        if (from.equals(to))
            return; // Speler is niet van chunk veranderd

        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        Clan playerClan = data.getClan();
        if (playerClan == null)
            return;

        ClaimManager claimManager = plugin.getClaimManager();

        String fromOwner = claimManager.getClaimOwner(from);
        String toOwner = claimManager.getClaimOwner(to);

        String clanName = playerClan.getName();

        // Speler betreedt zijn eigen claim
        if (toOwner != null && toOwner.equalsIgnoreCase(clanName)
                && (fromOwner == null || !fromOwner.equalsIgnoreCase(clanName))) {
            String msg = "§aNow Entering §7your clan territory §8[" + clanName + "]";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
        }

        // Speler verlaat zijn eigen claim
        if (fromOwner != null && fromOwner.equalsIgnoreCase(clanName)
                && (toOwner == null || !toOwner.equalsIgnoreCase(clanName))) {
            String msg = "§cNow Leaving §7your clan territory §8[" + clanName + "]";
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
        }
    }
}
