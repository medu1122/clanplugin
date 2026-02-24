package me.skibidi.clancore.gui;

import me.skibidi.clancore.flag.FlagManager;
import me.skibidi.clancore.flag.model.ClanFlag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Kho cờ 54 slot - chỉ được đặt/rút đá quý (gem material).
 */
public class FlagStorageGUI {

    public static final String TITLE_PREFIX = "§6Kho Cờ §7- ";

    public static void open(Player player, ClanFlag flag, FlagManager flagManager) {
        Map<Integer, Integer> amounts = flagManager.loadInventory(flag.getId());
        Material gem = flagManager.getGemMaterial();
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + flag.getClanName());

        for (int i = 0; i < 54; i++) {
            int amt = amounts.getOrDefault(i, 0);
            if (amt > 0) {
                ItemStack stack = new ItemStack(gem, Math.min(amt, gem.getMaxStackSize()));
                ItemMeta meta = stack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§6Đá quý §7(" + amt + ")");
                    if (amt > gem.getMaxStackSize())
                        meta.setLore(java.util.List.of("§7Tổng: " + amt));
                    stack.setItemMeta(meta);
                }
                inv.setItem(i, stack);
            }
        }

        player.openInventory(inv);
    }

    /** Chuyển inventory hiện tại về Map slot -> amount (chỉ đếm gem). */
    public static Map<Integer, Integer> inventoryToAmounts(Inventory inv, Material gem) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == gem && item.getAmount() > 0)
                map.put(i, item.getAmount());
        }
        return map;
    }
}
