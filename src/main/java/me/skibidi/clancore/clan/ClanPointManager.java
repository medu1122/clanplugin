package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
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
     * Bán vật phẩm lấy điểm đã bỏ. Nâng cấp clan sẽ dùng Đá quý (sẽ thêm sau).
     */
    public int sellItem(Player player, ItemStack item) {
        return 0;
    }

    /**
     * Nâng cấp clan: sẽ dùng Đá quý (chưa tích hợp). Không giới hạn level.
     */
    public boolean upgradeClan(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) return false;

        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể nâng cấp!");
            return false;
        }

        int currentLevel = clan.getLevel();
        if (!configManager.isLevelUnlimited() && currentLevel >= configManager.getMaxLevel()) {
            player.sendMessage("§cClan đã đạt level tối đa!");
            return false;
        }

        // Nâng cấp sẽ dùng Đá quý - tính năng sẽ thêm sau
        player.sendMessage("§eNâng cấp clan cần §6Đá quý§e. Tính năng đang được cập nhật.");
        return false;
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
