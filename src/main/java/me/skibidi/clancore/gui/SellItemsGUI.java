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

public class SellItemsGUI {

    private static final int[] SELL_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    public static void open(Player player, Clan clan, ConfigManager configManager, ClanPointManager pointManager) {
        Inventory inv = org.bukkit.Bukkit.createInventory(null, 54, "§6Bán Vật Phẩm");

        // Info panel ở dưới (row 4-5)
        updateInfoPanel(inv, clan, configManager, pointManager);

        // Glass panes decoration ở row 4-5
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        // Fill decoration slots
        for (int i = 36; i < 54; i++) {
            if (i != 40 && i != 49) { // Not info slot and close button
                inv.setItem(i, glass);
            }
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Quay lại");
            back.setItemMeta(backMeta);
        }
        inv.setItem(45, back);

        player.openInventory(inv);
    }

    public static void updateInfoPanel(Inventory inv, Clan clan, ConfigManager configManager, ClanPointManager pointManager) {
        // Calculate total value of items in sell slots
        int totalPoints = 0;
        List<String> itemList = new ArrayList<>();

        for (int slot : SELL_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Material material = item.getType();
                if (configManager.isSellable(material)) {
                    int pricePerItem = configManager.getSellPrice(material);
                    int amount = item.getAmount();
                    int itemPoints = pricePerItem * amount;
                    totalPoints += itemPoints;
                    String materialName = getMaterialDisplayName(material);
                    itemList.add("§7- §e" + amount + "x " + materialName + " §7= §e" + itemPoints + " điểm");
                }
            }
        }

        // Info item ở slot 40
        ItemStack infoItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Thông Tin Bán Hàng");
            List<String> lore = new ArrayList<>();
            lore.add("§7Nâng cấp clan sẽ dùng §6Đá quý§7. Tính năng đang cập nhật.");
            lore.add("");
            if (totalPoints > 0) {
                lore.add("§7Tổng giá trị:");
                lore.addAll(itemList);
                lore.add("");
                lore.add("§7Tổng điểm sẽ nhận: §e" + totalPoints);
                lore.add("");
                lore.add("§aClick vào đây để bán tất cả!");
            } else {
                lore.add("§7Đặt vật phẩm vào các ô phía trên");
                lore.add("§7để xem giá trị và bán chúng.");
            }
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(40, infoItem);
    }

    public static boolean isSellSlot(int slot) {
        for (int sellSlot : SELL_SLOTS) {
            if (slot == sellSlot) return true;
        }
        return false;
    }

    public static int[] getSellSlots() {
        return SELL_SLOTS.clone();
    }

    private static String getMaterialDisplayName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return result.toString();
    }
}
