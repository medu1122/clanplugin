package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.FlagManager;
import me.skibidi.clancore.flag.model.ClanFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * UI quản lý cờ - chỉ chủ clan hoặc admin. Gồm: phân quyền mở kho, mở kho, lấy cờ từ pool.
 */
public class FlagOwnerGUI {

    private static final int[] MEMBER_SLOTS = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25 };
    private static final int OPEN_STORAGE_SLOT = 31;
    private static final int TAKE_FLAG_SLOT = 40;
    private static final int INFO_SLOT = 4;
    private static final int CLOSE_SLOT = 49;

    public static void open(Player player, ClanFlag flag, Clan clan, ClanManager clanManager, FlagManager flagManager, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Quản Lý Cờ §7- §e" + flag.getClanName());

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) { gMeta.setDisplayName(" "); glass.setItemMeta(gMeta); }
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        long totalGems = flagManager.getTotalGems(flag.getId());
        ItemStack info = new ItemStack(Material.GOLD_INGOT);
        ItemMeta iMeta = info.getItemMeta();
        if (iMeta != null) {
            iMeta.setDisplayName("§6Kho Cờ §e" + flag.getClanName());
            iMeta.setLore(List.of(
                "§7Đá quý trong cờ: §e" + totalGems,
                "§7Dưới đây: thành viên có quyền §amở kho§7.",
                "§7Click tên để bật/tắt quyền."
            ));
            info.setItemMeta(iMeta);
        }
        inv.setItem(INFO_SLOT, info);

        Set<UUID> canOpen = flagManager.getCanOpenList(flag.getId());
        List<UUID> members = new ArrayList<>(clan.getMembers());
        int start = page * MEMBER_SLOTS.length;
        for (int i = 0; i < MEMBER_SLOTS.length && start + i < members.size(); i++) {
            UUID uuid = members.get(start + i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                String name = Bukkit.getOfflinePlayer(uuid).getName();
                meta.setDisplayName("§e" + (name != null ? name : uuid.toString().substring(0, 8)));
                meta.setLore(List.of(
                    canOpen.contains(uuid) ? "§aCó quyền mở kho" : "§cKhông có quyền",
                    "§7Click để đổi quyền"
                ));
                head.setItemMeta(meta);
            }
            inv.setItem(MEMBER_SLOTS[i], head);
        }

        ItemStack openBtn = new ItemStack(Material.CHEST);
        ItemMeta oMeta = openBtn.getItemMeta();
        if (oMeta != null) {
            oMeta.setDisplayName("§a§lMỞ KHO CỜ");
            oMeta.setLore(List.of("§7Click để mở kho (54 ô, chỉ đá quý)"));
            openBtn.setItemMeta(oMeta);
        }
        inv.setItem(OPEN_STORAGE_SLOT, openBtn);

        int available = flagManager.getAvailableFlagsToTake(clan);
        ItemStack takeBtn = new ItemStack(available > 0 ? Material.LIME_BANNER : Material.BARRIER);
        ItemMeta tMeta = takeBtn.getItemMeta();
        if (tMeta != null) {
            tMeta.setDisplayName(available > 0 ? "§a§lLẤY 1 CỜ §7(" + available + " có sẵn)" : "§cKhông còn cờ để lấy");
            tMeta.setLore(List.of(
                "§7Level 5 = 1 cờ, mỗi level sau +1.",
                "§7Lấy ra thì không bỏ lại được."
            ));
            takeBtn.setItemMeta(tMeta);
        }
        inv.setItem(TAKE_FLAG_SLOT, takeBtn);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cMeta = close.getItemMeta();
        if (cMeta != null) { cMeta.setDisplayName("§cĐóng"); close.setItemMeta(cMeta); }
        inv.setItem(CLOSE_SLOT, close);

        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta bMeta = back.getItemMeta();
            if (bMeta != null) { bMeta.setDisplayName("§7Trang trước"); back.setItemMeta(bMeta); }
            inv.setItem(45, back);
        }
        int totalPages = (int) Math.ceil((double) members.size() / MEMBER_SLOTS.length);
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nMeta = next.getItemMeta();
            if (nMeta != null) { nMeta.setDisplayName("§7Trang sau"); next.setItemMeta(nMeta); }
            inv.setItem(53, next);
        }

        player.openInventory(inv);
    }

    public static int[] getMemberSlots() { return MEMBER_SLOTS.clone(); }
    public static int getOpenStorageSlot() { return OPEN_STORAGE_SLOT; }
    public static int getTakeFlagSlot() { return TAKE_FLAG_SLOT; }
    public static int getCloseSlot() { return CLOSE_SLOT; }
}
