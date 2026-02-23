package me.skibidi.clancore.flag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.model.ClanFlag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FlagPlaceListener implements Listener {

    private final FlagManager flagManager;
    private final ClanManager clanManager;

    public FlagPlaceListener(FlagManager flagManager, ClanManager clanManager) {
        this.flagManager = flagManager;
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!FlagManager.isFlagItem(item)) return;

        String clanName = FlagManager.getClanNameFromFlagItem(item);
        if (clanName == null) return;

        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            player.sendMessage("§cClan §e" + clanName + " §ckhông tồn tại.");
            event.setCancelled(true);
            return;
        }
        if (!clan.canPlaceFlag()) {
            player.sendMessage("§cClan §e" + clanName + " §cchưa mở Base (cần level 5).");
            event.setCancelled(true);
            return;
        }

        Block base = block.getRelative(BlockFace.UP);
        if (base.getType() != Material.AIR) {
            player.sendMessage("§cCần không gian trống phía trên để cắm cờ.");
            event.setCancelled(true);
            return;
        }
        for (int i = 1; i <= FlagManager.POLE_HEIGHT + 1; i++) {
            if (block.getRelative(0, i, 0).getType() != Material.AIR) {
                player.sendMessage("§cCần không gian trống cao §e" + (FlagManager.POLE_HEIGHT + 1) + " §cblock để cắm cờ.");
                event.setCancelled(true);
                return;
            }
        }

        ClanFlag existing = flagManager.getFlagAt(block.getWorld(), block.getX(), block.getY(), block.getZ());
        if (existing != null) {
            player.sendMessage("§cĐã có cờ clan trong vùng này.");
            event.setCancelled(true);
            return;
        }

        int baseX = block.getX();
        int baseY = block.getY() + 1;
        int baseZ = block.getZ();
        String color = "RED";
        ClanFlag placed = flagManager.placeFlag(block.getWorld(), baseX, baseY, baseZ, clanName, color);
        if (placed != null) {
            item.setAmount(item.getAmount() - 1);
            player.sendMessage("§aĐã cắm cờ clan §e" + clanName + "§a! Vùng bán kính §e" + FlagManager.TERRITORY_RADIUS + " §ablock.");
            event.setCancelled(true);
        } else {
            player.sendMessage("§cKhông thể cắm cờ.");
        }
    }
}
