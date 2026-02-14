package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClanListGUI {

    private static final int CLANS_PER_PAGE = 28; // 7 rows x 4 columns
    private static final int[] CLAN_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static void open(Player player, ClanManager clanManager, int page) {
        Collection<Clan> allClans = clanManager.getAllClans();
        List<Clan> clansList = new ArrayList<>(allClans);
        
        if (clansList.isEmpty()) {
            player.sendMessage("§cKhông có clan nào trong server.");
            return;
        }

        int totalPages = (int) Math.ceil((double) clansList.size() / CLANS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, "§6Danh Sách Clans §7(Trang " + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Info item ở slot 4
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Thông Tin");
            List<String> lore = new ArrayList<>();
            lore.add("§7Tổng số clans: §e" + clansList.size());
            lore.add("§7Click vào clan để xem chi tiết");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Clan items
        int startIndex = page * CLANS_PER_PAGE;
        int endIndex = Math.min(startIndex + CLANS_PER_PAGE, clansList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Clan clan = clansList.get(i);
            int slot = CLAN_SLOTS[i - startIndex];

            // Đếm số member online
            int onlineCount = 0;
            int totalMembers = clan.getMembers().size();
            
            for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    onlineCount++;
                }
            }

            // Lấy leader info
            OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getOwner());
            String leaderName = leader.getName();
            if (leaderName == null) {
                leaderName = "Unknown (" + clan.getOwner().toString().substring(0, 8) + "...)";
            }
            boolean leaderOnline = leader.isOnline();

            ItemStack clanItem = new ItemStack(Material.WHITE_BANNER);
            ItemMeta meta = clanItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6" + clan.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Leader: " + (leaderOnline ? "§a" : "§c") + leaderName);
                lore.add("§7  " + (leaderOnline ? "§a● Online" : "§c● Offline"));
                lore.add("§7Members: §e" + totalMembers + "/" + clan.getMaxSlots());
                lore.add("§7Level: §e" + clan.getLevel());
                lore.add("");
                lore.add("§eClick để xem chi tiết!");
                meta.setLore(lore);
                clanItem.setItemMeta(meta);
            }
            inv.setItem(slot, clanItem);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Trang Trước");
                back.setItemMeta(backMeta);
            }
            inv.setItem(45, back);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Trang Sau");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(53, next);
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
