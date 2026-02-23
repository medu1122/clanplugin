package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.war.WarManager;
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

public class ClanWarGUI {

    private static final int CLANS_PER_PAGE = 28;
    private static final int[] CLAN_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static void open(Player player, Clan myClan, ClanManager clanManager, WarManager warManager, int page) {
        Collection<Clan> allClans = clanManager.getAllClans();
        List<Clan> others = new ArrayList<>();
        for (Clan c : allClans) {
            if (c != myClan) others.add(c);
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) others.size() / CLANS_PER_PAGE));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, "§cQuản Lý Chiến Tranh §7(Trang " + (page + 1) + "/" + totalPages + ")");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, glass);
        }

        ItemStack infoItem = new ItemStack(Material.REDSTONE);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6Chiến tranh với clan");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click vào đầu chủ clan để bật/tắt war.");
            lore.add("§7Chỉ khi §ccả hai §7đều bật mới là đang chiến tranh.");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        int startIndex = page * CLANS_PER_PAGE;
        for (int i = 0; i < CLANS_PER_PAGE && startIndex + i < others.size(); i++) {
            Clan target = others.get(startIndex + i);
            int slot = CLAN_SLOTS[i];

            OfflinePlayer owner = Bukkit.getOfflinePlayer(target.getOwner());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(owner);
                meta.setDisplayName("§6" + target.getName());
                List<String> lore = new ArrayList<>();
                boolean mySide = warManager.isWarEnabled(myClan, target);
                boolean theirSide = warManager.isWarEnabled(target, myClan);
                lore.add("§7Chủ clan: §e" + (owner.getName() != null ? owner.getName() : "?"));
                lore.add("§7Bạn bật war: " + (mySide ? "§cBẬT" : "§aTẮT"));
                lore.add("§7Họ bật war: " + (theirSide ? "§cBẬT" : "§aTẮT"));
                lore.add(mySide && theirSide ? "§c§lĐANG CHIẾN TRANH" : "§7Click để bật/tắt");
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot, head);
        }

        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Trang trước");
                back.setItemMeta(backMeta);
            }
            inv.setItem(45, back);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Trang sau");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(53, next);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        player.openInventory(inv);
    }

    public static int[] getClanSlots() {
        return CLAN_SLOTS.clone();
    }
}
