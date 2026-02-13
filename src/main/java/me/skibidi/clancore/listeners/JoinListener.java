package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.esp.EspManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinListener implements Listener {

    private final EspManager espManager;
    private final BuffManager buffManager;
    private final Plugin plugin;

    public JoinListener(EspManager espManager, BuffManager buffManager, Plugin plugin) {
        this.espManager = espManager;
        this.buffManager = buffManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        espManager.updateFor(event.getPlayer());
        // Apply buffs khi player join
        org.bukkit.entity.Player player = event.getPlayer();
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    // Check if player is still online before applying buffs
                    if (player.isOnline()) {
                        buffManager.applyBuffs(player);
                    }
                },
                20L // Delay 1 second để đảm bảo player đã load xong
        );
    }
}

