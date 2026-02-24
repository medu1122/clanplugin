package me.skibidi.clancore.gui;

import me.skibidi.clancore.flag.FlagManager;
import me.skibidi.clancore.flag.model.ClanFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * UI cho thành viên không có quyền mở kho - chỉ được nộp đá quý (nút lấy từ tay).
 */
public class FlagDonateGUI {

    private static final int DONATE_BUTTON_SLOT = 22;
    private static final int INFO_SLOT = 4;
    private static final int CLOSE_SLOT = 49;
    public static final String TITLE_PREFIX = "§6Nộp Đá Quý §7- ";

    public static void open(Player player, ClanFlag flag, FlagManager flagManager) {
        long total = flagManager.getTotalGems(flag.getId());
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + flag.getClanName());

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) { gMeta.setDisplayName(" "); glass.setItemMeta(gMeta); }
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        ItemStack info = new ItemStack(Material.GOLD_INGOT);
        ItemMeta iMeta = info.getItemMeta();
        if (iMeta != null) {
            iMeta.setDisplayName("§6Nộp đá quý");
            iMeta.setLore(List.of(
                "§7Đá quý trong cờ: §e" + total,
                "§7Cầm đá quý và click nút §aNộp §7bên dưới.",
                "§7Bạn không có quyền rút."
            ));
            info.setItemMeta(iMeta);
        }
        inv.setItem(INFO_SLOT, info);

        ItemStack donateBtn = new ItemStack(Material.EMERALD);
        ItemMeta dMeta = donateBtn.getItemMeta();
        if (dMeta != null) {
            dMeta.setDisplayName("§a§lNỘP ĐÁ QUÝ ĐANG CẦM");
            dMeta.setLore(List.of("§7Click để nộp toàn bộ đá quý đang cầm vào cờ."));
            donateBtn.setItemMeta(dMeta);
        }
        inv.setItem(DONATE_BUTTON_SLOT, donateBtn);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cMeta = close.getItemMeta();
        if (cMeta != null) { cMeta.setDisplayName("§cĐóng"); close.setItemMeta(cMeta); }
        inv.setItem(CLOSE_SLOT, close);

        player.openInventory(inv);
    }

    public static int getDonateButtonSlot() { return DONATE_BUTTON_SLOT; }
    public static int getCloseSlot() { return CLOSE_SLOT; }
}
