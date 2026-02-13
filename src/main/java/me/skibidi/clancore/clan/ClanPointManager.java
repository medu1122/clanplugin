package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClanPointManager {

    private final ClanManager clanManager;
    private final ConfigManager configManager;

    public ClanPointManager(ClanManager clanManager, ConfigManager configManager) {
        this.clanManager = clanManager;
        this.configManager = configManager;
    }

    /**
     * Sell item để lấy clan points.
     */
    public int sellItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;

        Clan clan = clanManager.getClan(player);
        if (clan == null) return 0;

        Material material = item.getType();
        if (!configManager.isSellable(material)) return 0;

        int pricePerItem = configManager.getSellPrice(material);
        int amount = item.getAmount();
        int totalPoints = pricePerItem * amount;

        clan.addClanPoints(totalPoints);
        if (!clanManager.updateClanStats(clan)) {
            // Rollback: remove points if DB save failed
            // Check return value to ensure rollback succeeded
            if (!clan.removeClanPoints(totalPoints)) {
                // Rollback failed - points may have been spent by another operation
                System.err.println("[ClanCore] CRITICAL: Failed to rollback clan points for clan: " + clan.getName() + 
                        ". Attempted to remove " + totalPoints + " points. Points may be in inconsistent state.");
                player.sendMessage("§cLỗi nghiêm trọng khi lưu điểm clan! Vui lòng liên hệ admin.");
            } else {
                player.sendMessage("§cLỗi khi lưu điểm clan! Vui lòng thử lại sau.");
            }
            return 0;
        }

        item.setAmount(0); // Remove item
        return totalPoints;
    }

    /**
     * Upgrade clan level nếu đủ điểm.
     */
    public boolean upgradeClan(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) return false;

        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể upgrade!");
            return false;
        }

        int currentLevel = clan.getLevel();
        if (currentLevel >= configManager.getMaxLevel()) {
            player.sendMessage("§cClan đã đạt level tối đa!");
            return false;
        }

        int requiredPoints = configManager.getUpgradeCost(currentLevel);
        
        // Check if upgrade cost is properly configured
        if (requiredPoints <= 0) {
            player.sendMessage("§cLỗi cấu hình! Chi phí upgrade cho level " + currentLevel + " không hợp lệ. Vui lòng liên hệ admin.");
            System.err.println("[ClanCore] ERROR: Upgrade cost for level " + currentLevel + " is not configured or invalid (value: " + requiredPoints + ")");
            return false;
        }
        
        if (clan.getClanPoints() < requiredPoints) {
            player.sendMessage("§cKhông đủ clan points! Cần: §e" + requiredPoints + " §cđiểm, hiện có: §e" + clan.getClanPoints());
            return false;
        }

        // Upgrade level - save points first, then upgrade
        int newLevel = currentLevel + 1;
        int oldPoints = clan.getClanPoints();
        
        // Remove points from in-memory - check return value to prevent race condition
        // If another thread spent points between check and removal, this will fail
        if (!clan.removeClanPoints(requiredPoints)) {
            // Points were spent by another operation between check and removal
            player.sendMessage("§cKhông đủ clan points! Điểm đã bị thay đổi. Hiện có: §e" + clan.getClanPoints());
            return false;
        }
        
        // Try to upgrade level (this will save to DB including deducted points)
        // If setClanLevel fails, we need to rollback points in BOTH memory and DB
        if (!clanManager.setClanLevel(clan, newLevel)) {
            // Rollback: restore points in memory FIRST (before attempting DB rollback)
            // This ensures in-memory state is correct even if DB rollback fails
            clan.setClanPoints(oldPoints);
            
            // Also rollback points in DB to ensure consistency
            // setClanLevel may have saved points to DB before failing
            // Save old points value before attempting rollback
            int rollbackPoints = oldPoints;
            if (!clanManager.updateClanStats(clan)) {
                // If DB rollback fails, log critical error
                // In-memory state is already restored, but DB may be inconsistent
                System.err.println("[ClanCore] CRITICAL: Failed to rollback clan points in database for clan: " + clan.getName() + 
                        ". In-memory points restored to " + rollbackPoints + ", but database may still have deducted points. " +
                        "Database and memory are now inconsistent. Please investigate.");
            }
            player.sendMessage("§cLỗi khi upgrade clan! Vui lòng thử lại sau.");
            return false;
        }

        // Update max slots theo level mới (already done in setClanLevel, but ensure it's set)
        ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(newLevel);
        if (levelConfig != null) {
            clan.setMaxSlots(levelConfig.getMaxMembers());
        }

        player.sendMessage("§aClan đã được upgrade lên level §e" + newLevel + "§a!");
        return true;
    }

    /**
     * Tính tổng buff speed và health của clan dựa trên level.
     */
    public int getTotalSpeedBuff(Clan clan) {
        if (clan == null) return 0;
        int total = 0;
        for (int level = 1; level <= clan.getLevel(); level++) {
            ConfigManager.LevelConfig config = configManager.getLevelConfig(level);
            if (config != null) {
                total += config.getSpeedBuff();
            }
        }
        return total;
    }

    public int getTotalHealthBuff(Clan clan) {
        if (clan == null) return 0;
        int total = 0;
        for (int level = 1; level <= clan.getLevel(); level++) {
            ConfigManager.LevelConfig config = configManager.getLevelConfig(level);
            if (config != null) {
                total += config.getHealthBuff();
            }
        }
        return total;
    }
}
