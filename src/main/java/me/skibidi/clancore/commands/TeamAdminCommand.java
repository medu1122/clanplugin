package me.skibidi.clancore.commands;

import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamAdminCommand implements CommandExecutor {

    private final TeamManager teamManager;

    public TeamAdminCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("clancore.admin")) {
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== Team Admin Commands ===");
            sender.sendMessage("§e/teamadmin tpall <người chơi> §7- Teleport tất cả thành viên team");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "tpall", "tp" -> handleTpAll(sender, args);
            default -> sender.sendMessage("§cLệnh không hợp lệ. Gõ §e/teamadmin §cđể xem danh sách lệnh.");
        }

        return true;
    }

    private void handleTpAll(CommandSender sender, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cCú pháp: §e/teamadmin tpall <người chơi>");
            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(args[1]);
        if (targetPlayer == null) {
            sender.sendMessage("§cKhông tìm thấy người chơi §e" + args[1] + "§c.");
            return;
        }

        Team team = teamManager.getTeam(targetPlayer);
        if (team == null) {
            sender.sendMessage("§cNgười chơi §e" + targetPlayer.getName() + " §ckhông ở trong team nào.");
            return;
        }

        Location targetLocation = admin.getLocation();
        int teleported = 0;

        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline() && !member.equals(admin)) {
                member.teleport(targetLocation);
                member.sendMessage("§6Bạn đã được teleport đến vị trí của admin!");
                teleported++;
            }
        }

        admin.sendMessage("§aĐã teleport §e" + teleported + " §athành viên team đến vị trí của bạn!");
    }
}
