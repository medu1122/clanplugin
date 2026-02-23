package me.skibidi.clancore.flag;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Chạy mỗi 20 tick: cập nhật player trong vùng cờ, damage 2s, effect vòng đỏ/xanh.
 */
public class FlagTerritoryTask extends BukkitRunnable {

    private final FlagManager flagManager;
    private final Plugin plugin;
    private int tick;

    public FlagTerritoryTask(FlagManager flagManager, Plugin plugin) {
        this.flagManager = flagManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOnline() || !player.isValid()) continue;
            flagManager.updatePlayerInFlags(player);
            flagManager.applyTerritoryDamage(player);
            flagManager.playAllyEffects(player);
        }
        tick++;
        if (tick % 2 == 0) {
            for (World world : Bukkit.getWorlds()) {
                flagManager.playTerritoryEffects(world);
            }
        }
    }

    public void start() {
        runTaskTimer(plugin, 20L, 20L);
    }
}
