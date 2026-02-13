package me.skibidi.clancore.listeners;

import me.skibidi.clancore.team.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvPListener implements Listener {

    private final TeamManager teamManager;

    public PvPListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        if (teamManager.sameTeam(damager, victim)) {
            event.setCancelled(true);
        }
    }
}

