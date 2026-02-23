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
            lore.add("");
            int cost = configManager.getUpgradeCost(clan.getLevel());
            lore.add("§7Yêu cầu nâng cấp (Đá quý): §e" + cost);
            lore.add("§7(Đá quý sẽ được tích hợp sau)");
            if (clan.getLevel() >= 5) {
                lore.add("");
                lore.add("§a§lĐÃ MỞ BASE (CỜ)");
            }
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

        // Ô đá quý (sắp ra mắt)
        ItemStack gemSlot = new ItemStack(Material.EMERALD);
        ItemMeta gemMeta = gemSlot.getItemMeta();
        if (gemMeta != null) {
            gemMeta.setDisplayName("§6§lĐÁ QUÝ");
            List<String> lore = new ArrayList<>();
            lore.add("§7Nâng cấp clan sẽ dùng §6Đá quý§7.");
            lore.add("§7Tính năng đang được cập nhật.");
            gemMeta.setLore(lore);
            gemSlot.setItemMeta(gemMeta);
        }
        inv.setItem(UPGRADE_SLOT, gemSlot);

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
            ConfigManager.LevelConfig nextConfig = configManager.getLevelConfig(clan.getLevel() + 1);
            if (nextConfig != null) {
                lore.add("");
                lore.add("§7Cấp độ tiếp theo " + (clan.getLevel() + 1) + ":");
                lore.add("§7- Số thành viên tối đa: §e" + nextConfig.getMaxMembers());
                lore.add("§7- Buff tốc độ thêm: §e+" + nextConfig.getSpeedBuff() + "%");
                lore.add("§7- Buff máu thêm: §e+" + nextConfig.getHealthBuff() + "%");
            }
            if (clan.getLevel() == 4) {
                lore.add("");
                lore.add("§aLevel 5: Mở Base (cờ)!");
            }
            benefitsMeta.setLore(lore);
            benefits.setItemMeta(benefitsMeta);
        }
        inv.setItem(40, benefits);

        // Upgrade button (đá quý - sắp ra mắt)
        int cost = configManager.getUpgradeCost(clan.getLevel());
        ItemStack upgradeButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta upgradeButtonMeta = upgradeButton.getItemMeta();
        if (upgradeButtonMeta != null) {
            upgradeButtonMeta.setDisplayName("§6§lNÂNG CẤP CLAN (ĐÁ QUÝ)");
            List<String> lore = new ArrayList<>();
            lore.add("§7Chi phí: §e" + cost + " §6Đá quý");
            lore.add("§7Tính năng đang được cập nhật.");
            upgradeButtonMeta.setLore(lore);
            upgradeButton.setItemMeta(upgradeButtonMeta);
        }
        inv.setItem(UPGRADE_BUTTON_SLOT, upgradeButton);

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
