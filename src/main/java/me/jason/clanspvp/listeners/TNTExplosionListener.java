package me.jason.clanspvp.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TNTExplosionListener implements Listener {

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        // Geen blokkade of bescherming nodig: TNT mag blokken breken, zelfs binnen
        // claims.
        // Deze listener is hier ter controle als je later iets zou willen loggen.
    }
}
