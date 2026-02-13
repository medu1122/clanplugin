package me.skibidi.clancore.gui;

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
import java.util.List;
import java.util.UUID;

public class ClanInfoGUI {

    private static final int MEMBERS_PER_PAGE = 28; // 7 rows x 4 columns (excluding navigation)
    private static final int[] MEMBER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static void open(Player player, Clan clan, int page) {
        List<UUID> members = new ArrayList<>(clan.getMembers());
        int totalPages = (int) Math.ceil((double) members.size() / MEMBERS_PER_PAGE);

        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, "§6Clan: §e" + clan.getName() + " §7(Page " + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Info item ở slot 4
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Clan Information");
            List<String> lore = new ArrayList<>();
            lore.add("§7Name: §e" + clan.getName());
            lore.add("§7Level: §e" + clan.getLevel());
            lore.add("§7Members: §e" + members.size() + "/" + clan.getMaxSlots());
            lore.add("§7Contribution: §e" + clan.getContribution());
            lore.add("§7Owner: §e" + Bukkit.getOfflinePlayer(clan.getOwner()).getName());
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Member heads
        int startIndex = page * MEMBERS_PER_PAGE;
        int endIndex = Math.min(startIndex + MEMBERS_PER_PAGE, members.size());

        for (int i = startIndex; i < endIndex; i++) {
            UUID memberUuid = members.get(i);
            int slot = MEMBER_SLOTS[i - startIndex];

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUuid);
            boolean isOnline = offlinePlayer.isOnline();
            Player onlinePlayer = offlinePlayer.getPlayer();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setDisplayName("§e" + offlinePlayer.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Status: " + (isOnline ? "§a● Online" : "§c● Offline"));
                if (clan.getOwner().equals(memberUuid)) {
                    lore.add("§6§lOwner");
                }
                if (isOnline && onlinePlayer != null) {
                    lore.add("§7Health: §c" + String.format("%.1f", onlinePlayer.getHealth()) + "§7/§c" + String.format("%.1f", onlinePlayer.getMaxHealth()));
                }
                skullMeta.setLore(lore);
                skullMeta.setOwningPlayer(offlinePlayer);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(slot, head);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Previous Page");
                back.setItemMeta(backMeta);
            }
            inv.setItem(45, back);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Next Page");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(53, next);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cClose");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        player.openInventory(inv);
    }
}
