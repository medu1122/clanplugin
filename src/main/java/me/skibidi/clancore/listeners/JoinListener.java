package me.skibidi.clancore.listeners;

import me.skibidi.clancore.esp.EspManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final EspManager espManager;

    public JoinListener(EspManager espManager) {
        this.espManager = espManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        espManager.updateFor(event.getPlayer());
    }
}

