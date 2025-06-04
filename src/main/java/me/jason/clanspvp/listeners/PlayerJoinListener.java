package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.utils.ClanScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ClansPvP plugin;

    public PlayerJoinListener() {
        this.plugin = ClansPvP.getInstance();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        if (data.getClan() != null) {
            ClanScoreboard.showClanScoreboard(player, data.getClan(), data);
        } else {
            ClanScoreboard.showNoClanScoreboard(player);
        }
    }
}
