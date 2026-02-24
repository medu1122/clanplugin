package me.skibidi.clancore.flag;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.flag.model.ClanFlag;
import me.skibidi.clancore.util.MessageUtil;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Xử lý phá block thuộc cột cờ:
 * - OP: cờ có máu (5x máu người chơi); mỗi lần phá trừ máu, khi hết máu mới cướp 5% đá (tối đa 300) và broadcast "đã bị tấn công".
 * - War + cờ đồng minh trong vùng địch: phá hoàn toàn → broadcast "đã bị phá hủy", toàn bộ đá rơi.
 * Chỉ 2 thông báo broadcast toàn server; còn lại chỉ gửi cho người thao tác (client).
 */
public class FlagBreakListener implements Listener {

    private final FlagManager flagManager;
    private final ClanManager clanManager;
    private final WarManager warManager;

    public FlagBreakListener(FlagManager flagManager, ClanManager clanManager, WarManager warManager) {
        this.flagManager = flagManager;
        this.clanManager = clanManager;
        this.warManager = warManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ClanFlag flag = flagManager.getFlagByBlock(
            event.getBlock().getWorld(),
            event.getBlock().getX(),
            event.getBlock().getY(),
            event.getBlock().getZ()
        );
        if (flag == null) return;

        event.setCancelled(true);
        event.setDropItems(false);
        Player player = event.getPlayer();

        // Chỉ OP mới được phá cờ kiểu "raid thường". Cờ có máu = 5x máu người chơi, tránh spam.
        if (player.isOp()) {
            int flagId = flag.getId();
            int hpLeft = flagManager.damageFlag(flagId, FlagManager.FLAG_DAMAGE_PER_HIT);
            if (hpLeft > 0) {
                // Chưa hết máu, chỉ trừ máu (không thông báo gì để tránh spam)
                return;
            }
            // Hết máu: raid 5% (nếu đủ 300 đá), broadcast "đã bị tấn công". Cờ không vỡ (event đã cancel) nên chỉ reset máu.
            long total = flagManager.getTotalGems(flagId);
            flagManager.resetFlagHealth(flagId);
            Bukkit.broadcastMessage("§eCờ của clan §6" + flag.getClanName() + "§e đã bị tấn công!");
            if (total < FlagManager.RAID_MAX_GEMS) {
                MessageUtil.sendFeedback(player, "§cKho cờ có dưới 300 đá quý. Không thể cướp thêm.");
                return;
            }
            int steal = (int) Math.min(Math.max(1, (total * FlagManager.RAID_PERCENT) / 100), FlagManager.RAID_MAX_GEMS);
            int taken = flagManager.removeGemsFromInventory(flagId, steal);
            if (taken > 0) {
                Material gem = flagManager.getGemMaterial();
                ItemStack give = new ItemStack(gem, taken);
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(give);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), give);
                }
                MessageUtil.sendFeedback(player, "§aĐã cướp §e" + taken + " §ađá quý từ cờ clan §6" + flag.getClanName() + "§a.");
            }
            return;
        }

        // Không phải OP: chỉ được phá hoàn toàn khi war + có cờ đồng minh trong vùng địch
        Clan playerClan = clanManager.getClan(player);
        if (playerClan == null) {
            MessageUtil.sendFeedback(player, "§cChỉ OP hoặc clan đang war (có cắm cờ trong vùng địch) mới phá được cờ.");
            return;
        }
        if (!warManager.isAtWar(playerClan, clanManager.getClan(flag.getClanName()))) {
            MessageUtil.sendFeedback(player, "§cClan bạn không ở chế độ chiến tranh với clan này. Không thể phá cờ.");
            return;
        }
        if (!flagManager.hasAllyFlagInRadius(flag, playerClan.getName())) {
            MessageUtil.sendFeedback(player, "§cPhải cắm cờ đồng minh trong vùng cờ địch mới được phá hủy hoàn toàn.");
            return;
        }

        String clanName = flag.getClanName();
        Location dropAt = flag.getY() > 0
            ? new Location(event.getBlock().getWorld(), flag.getX() + 0.5, flag.getY() + 0.5, flag.getZ() + 0.5)
            : event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        flagManager.destroyFlagCompletely(flag, dropAt);
        Bukkit.broadcastMessage("§c1 cờ của clan §6" + clanName + "§c đã bị phá hủy!");
    }
}
