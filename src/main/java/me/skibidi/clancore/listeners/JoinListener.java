package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.esp.EspManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinListener implements Listener {

    private final ClanManager clanManager;
    private final EspManager espManager;
    private final BuffManager buffManager;
    private final Plugin plugin;

    public JoinListener(EspManager espManager, BuffManager buffManager, Plugin plugin, ClanManager clanManager) {
        this.clanManager = clanManager;
        this.espManager = espManager;
        this.buffManager = buffManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        espManager.updateFor(player);
        clanManager.updatePlayerListName(player);
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (player.isOnline()) {
                        buffManager.applyBuffs(player);
                    }
                },
                20L
        );
    }
}

