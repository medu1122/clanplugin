package me.skibidi.clancore.commands;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanAdminCommand implements CommandExecutor {

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
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== Clan Admin Commands ===");
            sender.sendMessage("§e/clanadmin setlevel <clan> <level> §7- Set level cho clan");
            sender.sendMessage("§e/clanadmin tpall <clan> §7- Teleport tất cả thành viên clan");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "givepoints", "gp" -> handleGivePoints(sender, args);
            case "setlevel", "sl" -> handleSetLevel(sender, args);
            case "tpall", "tp" -> handleTpAll(sender, args);
            default -> sender.sendMessage("§cLệnh không hợp lệ. Gõ §e/clanadmin §cđể xem danh sách lệnh.");
        }

        return true;
    }

    private void handleGivePoints(CommandSender sender, String[] args) {
        sender.sendMessage("§eĐiểm clan đã bỏ. Nâng cấp clan sẽ dùng §6Đá quý§e (tính năng sẽ cập nhật).");
    }

    private void handleSetLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCú pháp: §e/clanadmin setlevel <clan> <level>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            sender.sendMessage("§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        try {
            int level = Integer.parseInt(args[2]);
            int maxLevel = configManager.getMaxLevel();
            if (level < 1 || level > maxLevel) {
                sender.sendMessage("§cLevel phải từ 1 đến " + maxLevel + "!");
                return;
            }

            clanManager.setClanLevel(clan, level);
            
            // Update max slots theo level mới
            ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(level);
            if (levelConfig != null) {
                clan.setMaxSlots(levelConfig.getMaxMembers());
            }

            sender.sendMessage("§aĐã set level clan §e" + clan.getName() + " §alên §e" + level + "§a!");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLevel không hợp lệ!");
        }
    }

    private void handleTpAll(CommandSender sender, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cCú pháp: §e/clanadmin tpall <clan>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            sender.sendMessage("§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        Location targetLocation = admin.getLocation();
        int teleported = 0;

        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline() && !member.equals(admin)) {
                member.teleport(targetLocation);
                member.sendMessage("§6Bạn đã được teleport đến vị trí của admin!");
                teleported++;
            }
        }

        admin.sendMessage("§aĐã teleport §e" + teleported + " §athành viên clan §e" + clan.getName() + " §ađến vị trí của bạn!");
    }
}
