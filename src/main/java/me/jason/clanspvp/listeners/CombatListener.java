package me.jason.clanspvp.listeners;

import me.jason.clanspvp.ClansPvP;
import me.jason.clanspvp.models.Clan;
import me.jason.clanspvp.models.PlayerData;
import me.jason.clanspvp.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final ClansPvP plugin;

    public CombatListener(ClansPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAllyDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }

        PlayerData attackerData = plugin.getPlayerData(attacker.getUniqueId());
        PlayerData victimData = plugin.getPlayerData(victim.getUniqueId());

        if (attackerData == null || victimData == null) {
            return;
        }

        Clan attackerClan = attackerData.getClan();
        Clan victimClan = victimData.getClan();

        if (attackerClan == null || victimClan == null) {
            return;
        }

        // ➤ Same clan: check friendly fire setting
        if (attackerClan.getName().equalsIgnoreCase(victimClan.getName())) {
            boolean allowClanFF = ConfigManager.get().getBoolean("clan-creation.allow-friendly-fire");
            if (!allowClanFF) {
                attacker.sendMessage(ConfigManager.getMessage("friendly-fire-blocked"));
                event.setCancelled(true);
            }
            return;
        }

        // ➤ Allied clans: check friendly fire between allies
        if (attackerClan.getAllies().contains(victimClan.getName())) {
            boolean allowAllyFF = ConfigManager.get().getBoolean("settings.allow-friendly-fire-between-allies");
            if (!allowAllyFF) {
                String msg = ConfigManager.getMessage("ally-friendly-fire-blocked")
                        .replace("%clan%", victimClan.getName());
                attacker.sendMessage(msg);
                event.setCancelled(true);
            }
        }
    }
}
