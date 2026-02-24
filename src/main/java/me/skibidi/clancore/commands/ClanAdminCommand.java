package me.skibidi.clancore.commands;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Phản hồi: Player = Action Bar, Console = sendMessage. */
public class ClanAdminCommand implements CommandExecutor {

    private static void feedback(CommandSender sender, String msg) {
        if (sender instanceof Player p) MessageUtil.sendFeedback(p, msg);
        else feedback(sender,msg);
    }

    private final ClanManager clanManager;
    private final ClanPointManager pointManager;
    private final ConfigManager configManager;

    public ClanAdminCommand(ClanManager clanManager, ClanPointManager pointManager, ConfigManager configManager) {
        this.clanManager = clanManager;
        this.pointManager = pointManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clancore.admin")) {
            feedback(sender,"§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            feedback(sender, "§e/clanadmin §7- setlevel, tpall, giveflag");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "givepoints", "gp" -> handleGivePoints(sender, args);
            case "setlevel", "sl" -> handleSetLevel(sender, args);
            case "tpall", "tp" -> handleTpAll(sender, args);
            case "giveflag", "gf" -> handleGiveFlag(sender, args);
            default -> feedback(sender,"§cLệnh không hợp lệ. Gõ §e/clanadmin §cđể xem danh sách lệnh.");
        }

        return true;
    }

    private void handleGivePoints(CommandSender sender, String[] args) {
        feedback(sender,"§eĐiểm clan đã bỏ. Nâng cấp clan sẽ dùng §6Đá quý§e (tính năng sẽ cập nhật).");
    }

    private void handleSetLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            feedback(sender,"§cCú pháp: §e/clanadmin setlevel <clan> <level>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            feedback(sender,"§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        try {
            int level = Integer.parseInt(args[2]);
            if (level < 1) {
                feedback(sender,"§cLevel phải >= 1!");
                return;
            }
            if (!configManager.isLevelUnlimited() && level > configManager.getMaxLevel()) {
                feedback(sender,"§cLevel tối đa là " + configManager.getMaxLevel() + "!");
                return;
            }

            clanManager.setClanLevel(clan, level);
            
            // Update max slots theo level mới
            ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(level);
            if (levelConfig != null) {
                clan.setMaxSlots(levelConfig.getMaxMembers());
            }

            feedback(sender,"§aĐã set level clan §e" + clan.getName() + " §alên §e" + level + "§a!");
        } catch (NumberFormatException e) {
            feedback(sender,"§cLevel không hợp lệ!");
        }
    }

    private void handleTpAll(CommandSender sender, String[] args) {
        if (!(sender instanceof Player admin)) {
            feedback(sender,"§cChỉ người chơi mới có thể sử dụng lệnh này.");
            return;
        }

        if (args.length < 2) {
            feedback(sender,"§cCú pháp: §e/clanadmin tpall <clan>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            feedback(sender,"§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        Location targetLocation = admin.getLocation();
        int teleported = 0;

        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline() && !member.equals(admin)) {
                member.teleport(targetLocation);
                MessageUtil.sendFeedback(member, "§6Bạn đã được teleport đến vị trí của admin!");
                teleported++;
            }
        }

        feedback(admin, "§aĐã teleport §e" + teleported + " §athành viên clan §e" + clan.getName() + " §ađến vị trí của bạn!");
    }

    private void handleGiveFlag(CommandSender sender, String[] args) {
        if (args.length < 2) {
            feedback(sender,"§cCú pháp: §e/clanadmin giveflag <player> [tên clan]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            feedback(sender,"§cNgười chơi không online.");
            return;
        }
        String clanName = args.length >= 3 ? args[2] : (clanManager.getClan(target) != null ? clanManager.getClan(target).getName() : null);
        if (clanName == null) {
            feedback(sender,"§cCần chỉ rõ tên clan hoặc người chơi phải đang trong clan.");
            return;
        }
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            feedback(sender,"§cKhông tìm thấy clan §e" + clanName + "§c.");
            return;
        }
        if (!clan.canPlaceFlag()) {
            feedback(sender,"§cClan §e" + clanName + " §cchưa mở Base (cần level 5).");
            return;
        }
        org.bukkit.inventory.ItemStack flag = me.skibidi.clancore.flag.FlagManager.createFlagItem(clanName, "RED");
        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), flag);
            feedback(sender,"§aĐã thả 1 cờ clan §e" + clanName + " §atại chân §e" + target.getName() + "§a (inventory đầy).");
        } else {
            target.getInventory().addItem(flag);
            feedback(sender,"§aĐã cho §e" + target.getName() + " §a1 cờ clan §e" + clanName + "§a.");
        }
    }
}
