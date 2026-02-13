package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ClanUpgradeGUI {

    private static final int UPGRADE_SLOT = 22; // Center slot để bỏ item
    private static final int UPGRADE_BUTTON_SLOT = 31; // Button để upgrade

    public static void open(Player player, Clan clan, ConfigManager configManager, ClanPointManager pointManager) {
        Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, "§6Nâng Cấp Clan");

        // Info panel ở top
        ItemStack infoItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Cấp Độ Clan: §e" + clan.getLevel());
            List<String> lore = new ArrayList<>();
            lore.add("§7Cấp độ hiện tại: §e" + clan.getLevel());
            lore.add("§7Điểm clan: §e" + clan.getClanPoints());
            lore.add("");
            if (clan.getLevel() < configManager.getMaxLevel()) {
                int requiredPoints = configManager.getUpgradeCost(clan.getLevel());
                lore.add("§7Yêu cầu nâng cấp:");
                lore.add("§7- Điểm clan: §e" + requiredPoints);
            } else {
                lore.add("§c§lĐÃ ĐẠT CẤP TỐI ĐA!");
            }
            lore.add("");
            lore.add("§7Đặt các vật phẩm có thể bán vào");
            lore.add("§e§lÔ NÂNG CẤP §7bên dưới để bán!");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Glass panes decoration
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        // Top row decoration
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                inv.setItem(i, glass);
            }
        }

        // Sell items button
        ItemStack sellButton = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta sellMeta = sellButton.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName("§6§lBÁN VẬT PHẨM");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click để mở GUI bán vật phẩm");
            lore.add("§7và đóng góp điểm cho clan!");
            sellMeta.setLore(lore);
            sellButton.setItemMeta(sellMeta);
        }
        inv.setItem(UPGRADE_SLOT, sellButton);

        // Level benefits display
        ItemStack benefits = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta benefitsMeta = benefits.getItemMeta();
        if (benefitsMeta != null) {
            benefitsMeta.setDisplayName("§6Lợi Ích Cấp Độ");
            List<String> lore = new ArrayList<>();
            ConfigManager.LevelConfig currentConfig = configManager.getLevelConfig(clan.getLevel());
            if (currentConfig != null) {
                lore.add("§7Cấp độ hiện tại " + clan.getLevel() + ":");
                lore.add("§7- Số thành viên tối đa: §e" + currentConfig.getMaxMembers());
                lore.add("§7- Buff tốc độ tổng: §e+" + pointManager.getTotalSpeedBuff(clan) + "%");
                lore.add("§7- Buff máu tổng: §e+" + pointManager.getTotalHealthBuff(clan) + "%");
            }
            if (clan.getLevel() < configManager.getMaxLevel()) {
                ConfigManager.LevelConfig nextConfig = configManager.getLevelConfig(clan.getLevel() + 1);
                if (nextConfig != null) {
                    lore.add("");
                    lore.add("§7Cấp độ tiếp theo " + (clan.getLevel() + 1) + ":");
                    lore.add("§7- Số thành viên tối đa: §e" + nextConfig.getMaxMembers());
                    lore.add("§7- Buff tốc độ thêm: §e+" + nextConfig.getSpeedBuff() + "%");
                    lore.add("§7- Buff máu thêm: §e+" + nextConfig.getHealthBuff() + "%");
                }
            }
            benefitsMeta.setLore(lore);
            benefits.setItemMeta(benefitsMeta);
        }
        inv.setItem(40, benefits);

        // Upgrade button
        if (clan.getLevel() < configManager.getMaxLevel()) {
            int requiredPoints = configManager.getUpgradeCost(clan.getLevel());
            boolean canUpgrade = clan.getClanPoints() >= requiredPoints;
            
            ItemStack upgradeButton = new ItemStack(canUpgrade ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
            ItemMeta upgradeButtonMeta = upgradeButton.getItemMeta();
            if (upgradeButtonMeta != null) {
                upgradeButtonMeta.setDisplayName(canUpgrade ? "§a§lNÂNG CẤP CLAN" : "§c§lKHÔNG THỂ NÂNG CẤP");
                List<String> lore = new ArrayList<>();
                lore.add("§7Điểm cần: §e" + requiredPoints);
                lore.add("§7Điểm hiện có: §e" + clan.getClanPoints());
                if (canUpgrade) {
                    lore.add("");
                    lore.add("§aClick để nâng cấp!");
                } else {
                    lore.add("");
                    lore.add("§cCần thêm §e" + (requiredPoints - clan.getClanPoints()) + " §cđiểm!");
                }
                upgradeButtonMeta.setLore(lore);
                upgradeButton.setItemMeta(upgradeButtonMeta);
            }
            inv.setItem(UPGRADE_BUTTON_SLOT, upgradeButton);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        player.openInventory(inv);
    }

}
