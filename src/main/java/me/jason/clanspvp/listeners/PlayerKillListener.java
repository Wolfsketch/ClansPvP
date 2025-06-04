package me.jason.clanspvp.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;

public class PlayerKillListener implements Listener {

    private final ClansPvP plugin = ClansPvP.getInstance();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim))
            return;

        PlayerData killerData = plugin.getPlayerData(killer.getUniqueId());
        killerData.addKill(); // verhoog kill count

        // Optioneel: hier later clan power verhogen met PowerManager
    }
}
