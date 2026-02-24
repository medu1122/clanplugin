package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ClanPointManager {

    private final ClanManager clanManager;
    private final ConfigManager configManager;
    /** MoneyPlugin instance khi có (soft dependency) – dùng Đá Quý Shard cho nâng cấp. */
    private final Object moneyPluginRef;

    public ClanPointManager(ClanManager clanManager, ConfigManager configManager, Object moneyPluginRef) {
        this.clanManager = clanManager;
        this.configManager = configManager;
        this.moneyPluginRef = moneyPluginRef;
    }

    public boolean hasMoneyPlugin() {
        return moneyPluginRef != null;
    }

    /**
     * Đếm số Đá Quý Shard trong túi (chỉ khi có MoneyPlugin).
     */
    public int countShardsInInventory(Player player) {
        if (moneyPluginRef == null) return 0;
        try {
            Method isShard = moneyPluginRef.getClass().getMethod("isCurrencyShard", ItemStack.class);
            int total = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir() && Boolean.TRUE.equals(isShard.invoke(moneyPluginRef, item)))
                    total += item.getAmount();
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Trừ amount Đá Quý Shard từ túi. Trả về true nếu đã trừ đủ.
     */
    public boolean removeShardsFromInventory(Player player, int amount) {
        if (moneyPluginRef == null || amount <= 0) return false;
        try {
            Method isShard = moneyPluginRef.getClass().getMethod("isCurrencyShard", ItemStack.class);
            int left = amount;
            for (int i = 0; i < player.getInventory().getSize() && left > 0; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item == null || item.getType().isAir()) continue;
                if (!Boolean.TRUE.equals(isShard.invoke(moneyPluginRef, item))) continue;
                int take = Math.min(item.getAmount(), left);
                if (item.getAmount() == take) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - take);
                }
                left -= take;
            }
            return left == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bán vật phẩm lấy điểm đã bỏ. Nâng cấp clan dùng Đá Quý Shard (MoneyPlugin).
     */
    public int sellItem(Player player, ItemStack item) {
        return 0;
    }

    /**
     * Nâng cấp clan: trừ Đá Quý Shard trong túi (khi có MoneyPlugin). Không giới hạn level.
     */
    public boolean upgradeClan(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) return false;

        if (!clanManager.isOwner(player, clan)) {
            MessageUtil.sendFeedback(player, "§cChỉ chủ clan mới có thể nâng cấp!");
            return false;
        }

        int currentLevel = clan.getLevel();
        if (!configManager.isLevelUnlimited() && currentLevel >= configManager.getMaxLevel()) {
            MessageUtil.sendFeedback(player, "§cClan đã đạt level tối đa!");
            return false;
        }

        int cost = configManager.getUpgradeCost(currentLevel);
        if (moneyPluginRef == null) {
            MessageUtil.sendFeedback(player, "§eNâng cấp clan cần §6Đá Quý Shard§e (MoneyPlugin). Tính năng chưa khả dụng.");
            return false;
        }

        int have = countShardsInInventory(player);
        if (have < cost) {
            MessageUtil.sendFeedback(player, "§cBạn cần §e" + cost + " §cĐá Quý Shard. Hiện có: §e" + have);
            return false;
        }

        if (!removeShardsFromInventory(player, cost)) {
            MessageUtil.sendFeedback(player, "§cKhông thể trừ Đá Quý Shard.");
            return false;
        }

        if (!clanManager.setClanLevel(clan, currentLevel + 1)) {
            MessageUtil.sendFeedback(player, "§cNâng cấp thất bại.");
            return false;
        }
        MessageUtil.sendFeedback(player, "§aĐã nâng cấp clan lên cấp §e" + clan.getLevel() + "§a!");
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
