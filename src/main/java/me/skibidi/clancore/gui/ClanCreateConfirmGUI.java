package me.skibidi.clancore.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * UI xác nhận trước khi trừ Đá Quý Shard để tạo clan.
 */
public class ClanCreateConfirmGUI {

    public static final int CREATE_CLAN_SHARD_COST = 100;
    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;
    public static final String TITLE = "§5Xác Nhận Tạo Clan";

    public static void open(Player player, String clanName, int costShards) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        ItemStack info = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§5§lTạo clan tốn Đá Quý Shard");
            infoMeta.setLore(List.of(
                "§7Tên clan: §e" + clanName,
                "",
                "§7Số §5Đá Quý Shard §7sẽ trừ: §e" + costShards,
                "",
                "§c⚠ Bạn có chắc muốn chi tiền để tạo clan?",
                "§cKhông thể hoàn tác!"
            ));
            info.setItemMeta(infoMeta);
        }
        inv.setItem(13, info);

        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§a§lXÁC NHẬN");
            confirmMeta.setLore(List.of("§7Click để dùng §e" + costShards + " §5Shard §7và tạo clan §e" + clanName));
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
