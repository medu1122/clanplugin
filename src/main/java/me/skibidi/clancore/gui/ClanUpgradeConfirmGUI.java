package me.skibidi.clancore.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * UI xác nhận trước khi trừ Đá Quý Shard để nâng cấp clan.
 */
public class ClanUpgradeConfirmGUI {

    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;
    public static final String TITLE = "§5Xác Nhận Nâng Cấp Clan";

    public static void open(Player player, String clanName, int currentLevel, int nextLevel, int costShards) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        ItemStack info = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§5§lDùng Đá Quý Shard");
            List<String> lore = new ArrayList<>();
            lore.add("§7Clan: §e" + clanName);
            lore.add("§7Cấp hiện tại: §e" + currentLevel + " §7→ §a" + nextLevel);
            lore.add("");
            lore.add("§7Số §5Đá Quý Shard §7sẽ trừ: §e" + costShards);
            lore.add("");
            lore.add("§c⚠ Bạn có chắc muốn dùng số Shard này?");
            lore.add("§cKhông thể hoàn tác!");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inv.setItem(13, info);

        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§a§lXÁC NHẬN");
            confirmMeta.setLore(List.of("§7Click để dùng §e" + costShards + " §5Shard §7và nâng cấp"));
            confirm.setItemMeta(confirmMeta);
        }
        inv.setItem(CONFIRM_SLOT, confirm);

        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§c§lHỦY BỎ");
            cancelMeta.setLore(List.of("§7Click để hủy, không trừ Shard"));
            cancel.setItemMeta(cancelMeta);
        }
        inv.setItem(CANCEL_SLOT, cancel);

        player.openInventory(inv);
    }

    public static boolean isConfirmSlot(int slot) {
        return slot == CONFIRM_SLOT;
    }

    public static boolean isCancelSlot(int slot) {
        return slot == CANCEL_SLOT;
    }
}
