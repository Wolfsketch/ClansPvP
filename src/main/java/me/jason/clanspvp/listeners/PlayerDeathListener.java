package me.jason.clanspvp.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.PlayerData;

public class PlayerDeathListener implements Listener {

    private final ClansPvP plugin = ClansPvP.getInstance();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player player = event.getEntity();
        PlayerData data = plugin.getPlayerData(player.getUniqueId());
        data.addDeath(); // verhoog death count

        // Optioneel: je kunt hier ook clan power verminderen of berichten tonen
    }
}
