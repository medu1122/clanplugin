package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.team.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final TeamManager teamManager;
    private final EspManager espManager;
    private final BuffManager buffManager;

    public QuitListener(TeamManager teamManager, EspManager espManager, BuffManager buffManager) {
        this.teamManager = teamManager;
        this.espManager = espManager;
        this.buffManager = buffManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teamManager.removeFromTeam(event.getPlayer());
        espManager.clear(event.getPlayer());
        buffManager.removeBuffs(event.getPlayer());
    }
}

