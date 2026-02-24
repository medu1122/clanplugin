package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.FlagManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI Kho Cờ (pool): cờ nhận khi lên level, chủ clan lấy ra tuỳ ý (không bỏ lại được).
 */
public class FlagPoolGUI {

    public static final String TITLE_PREFIX = "§6Kho Cờ §7- §e";

    private static final int SLOT_INFO = 4;
    private static final int SLOT_TAKE = 22;
    private static final int SLOT_CLOSE = 49;

    public static void open(Player player, Clan clan, FlagManager flagManager) {
        if (clan == null || flagManager == null) return;
        int available = flagManager.getAvailableFlagsToTake(clan);
        String title = TITLE_PREFIX + clan.getName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) { gMeta.setDisplayName(" "); glass.setItemMeta(gMeta); }
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        ItemStack info = new ItemStack(Material.GOLD_INGOT);
        ItemMeta iMeta = info.getItemMeta();
        if (iMeta != null) {
            iMeta.setDisplayName("§6Kho Cờ §7(Pool)");
            iMeta.setLore(List.of(
                "§7Cờ nhận khi clan lên level (từ level 5).",
                "§7Cờ có thể lấy: §e§l" + available,
                "§7Lấy ra thì không bỏ lại được."
            ));
            info.setItemMeta(iMeta);
        }
        inv.setItem(SLOT_INFO, info);

        ItemStack takeBtn = new ItemStack(available > 0 ? Material.LIME_BANNER : Material.BARRIER);
        ItemMeta tMeta = takeBtn.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(available > 0 ? "§a§lLẤY 1 CỜ §7(" + available + " có sẵn)" : "§cKhông còn cờ để lấy");
            tMeta.setLore(List.of("§7Level 5 = 1 cờ, mỗi level sau +1."));
            takeBtn.setItemMeta(tMeta);
        }
        inv.setItem(SLOT_TAKE, takeBtn);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cMeta = close.getItemMeta();
        if (cMeta != null) { cMeta.setDisplayName("§cĐóng"); close.setItemMeta(cMeta); }
        inv.setItem(SLOT_CLOSE, close);

        player.openInventory(inv);
    }

    public static int getTakeSlot() { return SLOT_TAKE; }
    public static int getCloseSlot() { return SLOT_CLOSE; }
}
