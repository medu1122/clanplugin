package me.skibidi.clancore.gui;

import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.flag.FlagManager;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Menu chính khi gõ /clan (không args). Khác nhau theo vai trò: không clan / member / chủ clan.
 */
public class ClanMenuGUI {

    public static final String TITLE = "§6Clan §7- §eMenu";

    public static final int SLOT_INFO = 11;
    public static final int SLOT_UPGRADE = 13;
    public static final int SLOT_WAR = 15;
    public static final int SLOT_FLAG_POOL = 20; // Kho Cờ (level 5+)
    public static final int SLOT_LIST = 22;
    public static final int SLOT_CREATE = 13;  // khi chưa có clan
    public static final int SLOT_CLOSE = 26;

    public static void open(Player player, Clan clan, boolean isOwner, ConfigManager configManager, ClanPointManager pointManager, WarManager warManager, Object moneyPluginRef, FlagManager flagManager) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        if (clan == null) {
            ItemStack create = new ItemStack(Material.PAPER);
            ItemMeta m = create.getItemMeta();
            if (m != null) {
                m.setDisplayName("§a§lTạo clan mới");
                m.setLore(List.of("§7Tốn 100 Đá Quý Shard", "§7Dùng §e/clan create <tên>"));
                create.setItemMeta(m);
            }
            inv.setItem(SLOT_CREATE, create);
            ItemStack list = new ItemStack(Material.BOOK);
            ItemMeta lm = list.getItemMeta();
            if (lm != null) {
                lm.setDisplayName("§e§lDanh sách clans");
                lm.setLore(List.of("§7Xem các clan trong server"));
                list.setItemMeta(lm);
            }
            inv.setItem(SLOT_LIST, list);
        } else {
            ItemStack info = new ItemStack(Material.BOOK);
            ItemMeta im = info.getItemMeta();
            if (im != null) {
                im.setDisplayName("§6§lThông tin clan");
                im.setLore(List.of("§7Clan: §e" + clan.getName(), "§7Cấp: §e" + clan.getLevel()));
                info.setItemMeta(im);
            }
            inv.setItem(SLOT_INFO, info);

            if (isOwner) {
                ItemStack up = new ItemStack(Material.AMETHYST_SHARD);
                ItemMeta um = up.getItemMeta();
                if (um != null) {
                    um.setDisplayName("§5§lNâng cấp clan");
                    um.setLore(List.of("§7Dùng Đá Quý Shard"));
                    up.setItemMeta(um);
                }
                inv.setItem(SLOT_UPGRADE, up);

                ItemStack war = new ItemStack(Material.IRON_SWORD);
                ItemMeta wm = war.getItemMeta();
                if (wm != null) {
                    wm.setDisplayName("§c§lQuản lý chiến tranh");
                    wm.setLore(List.of("§7Bật/tắt war với từng clan"));
                    war.setItemMeta(wm);
                }
                inv.setItem(SLOT_WAR, war);

                if (clan.getLevel() >= 5 && flagManager != null) {
                    ItemStack flagPool = new ItemStack(Material.CHEST);
                    ItemMeta fpMeta = flagPool.getItemMeta();
                    if (fpMeta != null) {
                        int available = flagManager.getAvailableFlagsToTake(clan);
                        fpMeta.setDisplayName("§6§lKho Cờ §7(Pool)");
                        fpMeta.setLore(List.of("§7Cờ nhận khi lên level. Có thể lấy: §e" + available));
                        flagPool.setItemMeta(fpMeta);
                    }
                    inv.setItem(SLOT_FLAG_POOL, flagPool);
                }
            }

            ItemStack list = new ItemStack(Material.BOOK);
            ItemMeta lm = list.getItemMeta();
            if (lm != null) {
                lm.setDisplayName("§e§lDanh sách clans");
                lm.setLore(List.of("§7Xem các clan trong server"));
                list.setItemMeta(lm);
            }
            inv.setItem(SLOT_LIST, list);
        }

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) cm.setDisplayName("§cĐóng");
        close.setItemMeta(cm);
        inv.setItem(SLOT_CLOSE, close);

        player.openInventory(inv);
    }
}
