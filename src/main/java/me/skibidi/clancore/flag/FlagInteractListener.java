package me.skibidi.clancore.flag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.model.ClanFlag;
import me.skibidi.clancore.gui.FlagDonateGUI;
import me.skibidi.clancore.gui.FlagOwnerGUI;
import me.skibidi.clancore.gui.FlagStorageGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Right-click lên block cờ mở UI (owner/member/donate). Xử lý click trong GUI và đóng kho (lưu, chỉ gem).
 */
public class FlagInteractListener implements Listener {

    private final FlagManager flagManager;
    private final ClanManager clanManager;
    private final Map<UUID, Integer> ownerGuiFlagId = new HashMap<>();
    private final Map<UUID, Integer> ownerGuiPage = new HashMap<>();
    private final Map<UUID, Integer> storageGuiFlagId = new HashMap<>();
    private final Map<UUID, Integer> donateGuiFlagId = new HashMap<>();

    public FlagInteractListener(FlagManager flagManager, ClanManager clanManager) {
        this.flagManager = flagManager;
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (FlagManager.isFlagItem(hand)) return;

        ClanFlag flag = flagManager.getFlagByBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
        if (flag == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        Clan clan = clanManager.getClan(player);
        boolean isOwner = clan != null && clan.getOwner().equals(player.getUniqueId());
        boolean isAdmin = player.isOp() || player.hasPermission("clancore.admin");

        if (clan == null || !clan.getName().equals(flag.getClanName())) {
            player.sendMessage("§cBạn không thuộc clan sở hữu cờ này.");
            return;
        }

        if (isOwner || isAdmin) {
            FlagOwnerGUI.open(player, flag, clan, clanManager, flagManager, 0);
            ownerGuiFlagId.put(player.getUniqueId(), flag.getId());
            ownerGuiPage.put(player.getUniqueId(), 0);
        } else if (flagManager.canOpenStorage(flag.getId(), player.getUniqueId())) {
            FlagStorageGUI.open(player, flag, flagManager);
            storageGuiFlagId.put(player.getUniqueId(), flag.getId());
        } else {
            FlagDonateGUI.open(player, flag, flagManager);
            donateGuiFlagId.put(player.getUniqueId(), flag.getId());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.startsWith("§6Quản Lý Cờ §7- §e")) {
            event.setCancelled(true);
            Integer flagId = ownerGuiFlagId.get(player.getUniqueId());
            if (flagId == null) return;
            ClanFlag flag = flagManager.getFlagById(flagId);
            Clan clan = clanManager.getClan(player);
            if (flag == null || clan == null) return;
            int page = ownerGuiPage.getOrDefault(player.getUniqueId(), 0);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (event.getSlot() == FlagOwnerGUI.getCloseSlot()) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == 45 && clicked.getType() == Material.ARROW && page > 0) {
                ownerGuiPage.put(player.getUniqueId(), page - 1);
                FlagOwnerGUI.open(player, flag, clan, clanManager, flagManager, page - 1);
                return;
            }
            if (event.getSlot() == 53 && clicked.getType() == Material.ARROW) {
                int totalPages = (int) Math.ceil((double) clan.getMembers().size() / FlagOwnerGUI.getMemberSlots().length);
                if (page < totalPages - 1) {
                    ownerGuiPage.put(player.getUniqueId(), page + 1);
                    FlagOwnerGUI.open(player, flag, clan, clanManager, flagManager, page + 1);
                }
                return;
            }
            if (event.getSlot() == FlagOwnerGUI.getOpenStorageSlot()) {
                player.closeInventory();
                FlagStorageGUI.open(player, flag, flagManager);
                storageGuiFlagId.put(player.getUniqueId(), flag.getId());
                return;
            }
            if (event.getSlot() == FlagOwnerGUI.getTakeFlagSlot() && flagManager.getAvailableFlagsToTake(clan) > 0) {
                if (flagManager.takeFlagFromPool(clan, player)) {
                    player.sendMessage("§aĐã lấy 1 cờ từ kho.");
                    FlagOwnerGUI.open(player, flag, clan, clanManager, flagManager, page);
                }
                return;
            }
            int[] memberSlots = FlagOwnerGUI.getMemberSlots();
            for (int i = 0; i < memberSlots.length; i++) {
                if (event.getSlot() == memberSlots[i]) {
                    int idx = page * memberSlots.length + i;
                    if (idx >= clan.getMembers().size()) break;
                    UUID target = new java.util.ArrayList<>(clan.getMembers()).get(idx);
                    if (target.equals(clan.getOwner())) break;
                    boolean has = flagManager.getCanOpenList(flag.getId()).contains(target);
                    flagManager.setCanOpenStorage(flag.getId(), target, !has);
                    player.sendMessage("§aĐã " + (!has ? "cấp" : "thu hồi") + " quyền mở kho cho thành viên.");
                    FlagOwnerGUI.open(player, flag, clan, clanManager, flagManager, page);
                    break;
                }
            }
            return;
        }

        if (title.startsWith(FlagStorageGUI.TITLE_PREFIX)) {
            Integer flagId = storageGuiFlagId.get(player.getUniqueId());
            if (flagId == null) return;
            Material gem = flagManager.getGemMaterial();
            ItemStack cur = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (event.getRawSlot() >= 54) {
                if (cursor != null && !cursor.getType().isAir() && cursor.getType() != gem) {
                    event.setCancelled(true);
                    player.sendMessage("§cChỉ được đặt đá quý vào kho cờ!");
                }
                return;
            }
            if (cur != null && cur.getType() != Material.AIR && cur.getType() != gem) event.setCancelled(true);
            if (cursor != null && !cursor.getType().isAir() && cursor.getType() != gem) {
                event.setCancelled(true);
                player.sendMessage("§cChỉ được đặt đá quý vào kho cờ!");
            }
            return;
        }

        if (title.startsWith(FlagDonateGUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            Integer flagId = donateGuiFlagId.get(player.getUniqueId());
            if (flagId == null) return;
            if (event.getSlot() == FlagDonateGUI.getCloseSlot()) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == FlagDonateGUI.getDonateButtonSlot()) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                Material gem = flagManager.getGemMaterial();
                if (hand.getType() != gem || hand.getAmount() <= 0) {
                    player.sendMessage("§cCầm đá quý trong tay rồi click nút Nộp.");
                    return;
                }
                int amount = hand.getAmount();
                Map<Integer, Integer> inv = flagManager.loadInventory(flagId);
                int slot = 0;
                while (slot < 54 && inv.getOrDefault(slot, 0) > 0) slot++;
                if (slot >= 54) {
                    player.sendMessage("§cKho cờ đã đầy.");
                    return;
                }
                inv.put(slot, amount);
                flagManager.saveInventory(flagId, inv);
                hand.setAmount(0);
                player.sendMessage("§aĐã nộp §e" + amount + " §ađá quý vào cờ.");
                ClanFlag flag = flagManager.getFlagById(flagId);
                if (flag != null) FlagDonateGUI.open(player, flag, flagManager);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = event.getView().getTitle();
        UUID uuid = player.getUniqueId();

        if (title.startsWith(FlagStorageGUI.TITLE_PREFIX)) {
            Integer flagId = storageGuiFlagId.remove(uuid);
            if (flagId != null) {
                Map<Integer, Integer> amounts = FlagStorageGUI.inventoryToAmounts(event.getInventory(), flagManager.getGemMaterial());
                flagManager.saveInventory(flagId, amounts);
            }
            return;
        }

        if (title.startsWith(FlagDonateGUI.TITLE_PREFIX)) {
            donateGuiFlagId.remove(uuid);
            return;
        }

        if (title.startsWith("§6Quản Lý Cờ §7- §e")) {
            ownerGuiFlagId.remove(uuid);
            ownerGuiPage.remove(uuid);
        }
    }
}
