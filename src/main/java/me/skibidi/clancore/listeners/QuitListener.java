package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.team.TeamManager;
import org.bukkit.entity.Player;
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
        Player player = event.getPlayer();
        // Lưu team trước khi remove để update ESP cho các members còn lại
        me.skibidi.clancore.team.model.Team team = teamManager.getTeam(player);
        
        teamManager.removeFromTeam(player);
        espManager.clear(player);
        buffManager.removeBuffs(player);
        
        // Update ESP cho các members còn lại (nếu team vẫn tồn tại)
        if (team != null) {
            espManager.updateTeamMembers(team);
        }
    }
}

