package me.skibidi.clancore.commands;

import me.skibidi.clancore.chat.TeamChatManager;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.gui.TeamInfoGUI;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCommand implements CommandExecutor {

    private final TeamManager teamManager;
    private final TeamChatManager chatManager;
    private final EspManager espManager;

    public TeamCommand(TeamManager teamManager, TeamChatManager chatManager, EspManager espManager) {
        this.teamManager = teamManager;
        this.chatManager = chatManager;
        this.espManager = espManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này.");
            return true;
        }

        if (args.length == 0) {
            showHelpPage(player, 0);
            return true;
        }

        // Kiểm tra nếu là lệnh help với số trang
        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h")) {
            int page = 0;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]) - 1;
                } catch (NumberFormatException e) {
                    page = 0;
                }
            }
            showHelpPage(player, page);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create", "c" -> handleCreate(player);
            case "invite", "i" -> handleInvite(player, args);
            case "accept", "a" -> handleAccept(player);
            case "leave", "l" -> handleLeave(player);
            case "kick", "k" -> handleKick(player, args);
            case "disband", "d" -> handleDisband(player);
            case "info", "in" -> handleInfo(player);
            case "list", "li" -> handleList(player);
            case "transfer", "t" -> handleTransfer(player, args);
            case "taccept" -> handleTransferAccept(player);
            case "tdeny" -> handleTransferDeny(player);
            case "chat", "ch" -> handleChat(player, args);
            default -> {
                player.sendMessage("§cLệnh không hợp lệ. Gõ §e/team §cđể xem danh sách lệnh.");
                player.sendMessage("§7Hoặc dùng §e/team help <số trang> §7để xem các trang khác.");
            }
        }

        return true;
    }

    private void handleCreate(Player player) {
        // Kiểm tra phân quyền: player không được ở trong team nào mới có thể tạo team mới
        if (teamManager.isInTeam(player)) {
            player.sendMessage("§cBạn đã ở trong một team rồi. Dùng §e/team leave §cđể rời team hiện tại.");
            return;
        }
        if (teamManager.createTeam(player)) {
            player.sendMessage("§aĐã tạo team thành công!");
            // Update ESP cho player
            espManager.updateFor(player);
        } else {
            player.sendMessage("§cKhông thể tạo team. Vui lòng thử lại sau.");
        }
    }

    private void handleInvite(Player player, String[] args) {
        // Kiểm tra phân quyền: chỉ leader của team mới có thể invite
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }
        
        if (!team.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cChỉ leader của team mới có thể mời thành viên.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/team invite <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy người chơi §e" + args[1] + "§c.");
            return;
        }

        if (teamManager.invite(player, target)) {
            player.sendMessage("§aĐã mời §e" + target.getName() + " §avào team.");
            target.sendMessage("§6Bạn đã được mời tham gia team. Dùng §e/team accept §6để tham gia.");
            // Phát sound ping để thông báo
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            player.sendMessage("§cKhông thể mời người chơi này. Có thể bạn không phải leader hoặc người chơi đã ở trong team.");
        }
    }

    private void handleAccept(Player player) {
        Team teamBefore = teamManager.getTeam(player);
        if (teamManager.accept(player)) {
            player.sendMessage("§aBạn đã tham gia team thành công!");
            // Update ESP cho player và tất cả members của team
            Team team = teamManager.getTeam(player);
            if (team != null) {
                espManager.updateTeamMembers(team);
            }
        } else {
            player.sendMessage("§cBạn không có lời mời nào đang chờ xử lý.");
        }
    }

    private void handleLeave(Player player) {
        Team team = teamManager.getTeam(player);
        if (teamManager.leave(player)) {
            player.sendMessage("§cBạn đã rời khỏi team.");
            // Update ESP cho player (đã rời team) và các members còn lại
            espManager.updateFor(player);
            if (team != null) {
                espManager.updateTeamMembers(team);
            }
        } else {
            player.sendMessage("§cBạn không ở trong team nào.");
        }
    }

    private void handleKick(Player player, String[] args) {
        // Kiểm tra phân quyền: chỉ leader của team mới có thể kick
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }
        
        if (!team.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cChỉ leader của team mới có thể đuổi thành viên.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/team kick <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy người chơi §e" + args[1] + "§c.");
            return;
        }

        Team currentTeam = teamManager.getTeam(player);
        if (teamManager.kick(player, target)) {
            player.sendMessage("§aĐã đuổi §e" + target.getName() + " §akhỏi team.");
            target.sendMessage("§cBạn đã bị đuổi khỏi team.");
            // Update ESP cho target (đã bị kick) và các members còn lại
            espManager.updateFor(target);
            if (currentTeam != null) {
                espManager.updateTeamMembers(currentTeam);
            }
        } else {
            player.sendMessage("§cKhông thể đuổi người chơi này. Có thể bạn không phải leader hoặc người chơi không ở trong team của bạn.");
        }
    }

    private void handleDisband(Player player) {
        // Kiểm tra phân quyền: chỉ leader của team mới có thể disband
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }
        
        if (!team.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cChỉ leader của team mới có thể giải tán team.");
            return;
        }
        
        Team currentTeam = teamManager.getTeam(player);
        if (teamManager.disband(player)) {
            player.sendMessage("§cĐã giải tán team.");
            // Update ESP cho tất cả members (team đã disband)
            if (currentTeam != null) {
                espManager.updateTeamMembers(currentTeam);
            }
            espManager.updateFor(player);
        } else {
            player.sendMessage("§cKhông thể giải tán team.");
        }
    }

    private void handleInfo(Player player) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào!");
            return;
        }
        TeamInfoGUI.open(player, team, 0);
    }

    private void handleList(Player player) {
        java.util.Collection<Team> allTeams = teamManager.getAllTeams();
        
        // Lọc chỉ các team có ít nhất 1 member online
        java.util.List<Team> activeTeams = new java.util.ArrayList<>();
        for (Team team : allTeams) {
            boolean hasOnlineMember = false;
            // Create a copy to avoid ConcurrentModificationException
            for (java.util.UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    hasOnlineMember = true;
                    break;
                }
            }
            if (hasOnlineMember) {
                activeTeams.add(team);
            }
        }

        if (activeTeams.isEmpty()) {
            player.sendMessage("§cKhông có team nào đang hoạt động.");
            return;
        }

        player.sendMessage("§6=== Danh Sách Teams ===");
        int index = 1;
        for (Team team : activeTeams) {
            // Đếm số member online
            int onlineCount = 0;
            java.util.List<String> onlineMembers = new java.util.ArrayList<>();
            
            // Create a copy to avoid ConcurrentModificationException
            for (java.util.UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    onlineCount++;
                    onlineMembers.add(member.getName());
                }
            }

            if (onlineCount == 0) {
                continue; // Bỏ qua team không có member online
            }

            // Lấy leader name
            Player leaderPlayer = Bukkit.getPlayer(team.getLeader());
            String leaderName;
            if (leaderPlayer != null && leaderPlayer.isOnline()) {
                leaderName = "§a" + leaderPlayer.getName();
            } else {
                org.bukkit.OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(team.getLeader());
                String offlineName = offlineLeader.getName();
                if (offlineName == null) {
                    offlineName = "Unknown (" + team.getLeader().toString().substring(0, 8) + "...)";
                }
                leaderName = "§c" + offlineName + " (Offline)";
            }

            player.sendMessage("§e" + index + ". §7Leader: " + leaderName);
            player.sendMessage("   §7Members online (§a" + onlineCount + "§7): §f" + String.join("§7, §f", onlineMembers));
            index++;
        }
    }

    private void handleTransfer(Player player, String[] args) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ leader mới có thể transfer
        if (!team.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cChỉ leader mới có thể chuyển quyền sở hữu.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/team transfer <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cNgười chơi §e" + args[1] + " §ckhông online hoặc không tồn tại.");
            return;
        }

        if (!team.isMember(target.getUniqueId())) {
            player.sendMessage("§cNgười chơi này không phải thành viên của team.");
            return;
        }

        if (teamManager.requestTransferOwnership(team, target.getUniqueId())) {
            player.sendMessage("§aĐã gửi yêu cầu chuyển quyền sở hữu team cho §e" + target.getName() + "§a!");
            target.sendMessage("§6" + player.getName() + " §eđã gửi yêu cầu chuyển quyền leader team cho bạn!");
            target.sendMessage("§7Dùng §e/team taccept §7để chấp nhận hoặc §e/team tdeny §7để từ chối.");
            // Phát sound ping để thông báo
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            player.sendMessage("§cKhông thể gửi yêu cầu chuyển quyền sở hữu. Vui lòng thử lại sau.");
        }
    }

    private void handleTransferAccept(Player player) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }

        UUID pendingTransfer = team.getPendingTransferTo();
        if (pendingTransfer == null || !pendingTransfer.equals(player.getUniqueId())) {
            player.sendMessage("§cBạn không có yêu cầu chuyển quyền sở hữu nào.");
            return;
        }

        // Lưu UUID của old leader trước khi acceptTransferOwnership() thay đổi leader
        UUID oldLeaderUuid = team.getLeader();
        Player oldLeader = Bukkit.getPlayer(oldLeaderUuid);
        
        if (teamManager.acceptTransferOwnership(team)) {
            player.sendMessage("§aBạn đã trở thành leader mới của team!");
            if (oldLeader != null && oldLeader.isOnline()) {
                oldLeader.sendMessage("§a" + player.getName() + " §eđã chấp nhận yêu cầu chuyển quyền leader team!");
            }
            // Update ESP cho tất cả members (leader đã thay đổi)
            espManager.updateTeamMembers(team);
        } else {
            player.sendMessage("§cKhông thể chuyển quyền sở hữu. Vui lòng thử lại sau.");
        }
    }

    private void handleTransferDeny(Player player) {
        Team team = teamManager.getTeam(player);
        if (team == null) {
            player.sendMessage("§cBạn không ở trong team nào.");
            return;
        }

        UUID pendingTransfer = team.getPendingTransferTo();
        if (pendingTransfer == null) {
            player.sendMessage("§cBạn không có yêu cầu chuyển quyền sở hữu nào.");
            return;
        }

        UUID playerUuid = player.getUniqueId();
        boolean isRecipient = pendingTransfer.equals(playerUuid);
        boolean isLeader = team.getLeader().equals(playerUuid);

        // Allow both recipient and leader to cancel the transfer
        if (!isRecipient && !isLeader) {
            player.sendMessage("§cBạn không có quyền hủy yêu cầu chuyển quyền sở hữu này.");
            return;
        }

        // Cancel the transfer
        teamManager.cancelTransferOwnership(team);
        
        if (isRecipient) {
            // Recipient denied
            player.sendMessage("§cBạn đã từ chối yêu cầu chuyển quyền sở hữu team.");
            Player oldLeader = Bukkit.getPlayer(team.getLeader());
            if (oldLeader != null && oldLeader.isOnline()) {
                oldLeader.sendMessage("§c" + player.getName() + " §eđã từ chối yêu cầu chuyển quyền leader team.");
            }
        } else if (isLeader) {
            // Leader cancelled their own request
            player.sendMessage("§cBạn đã hủy yêu cầu chuyển quyền sở hữu team.");
            Player recipient = Bukkit.getPlayer(pendingTransfer);
            if (recipient != null && recipient.isOnline()) {
                recipient.sendMessage("§c" + player.getName() + " §eđã hủy yêu cầu chuyển quyền leader team.");
            }
        }
    }

    private void showHelpPage(Player player, int page) {
        java.util.List<String> commands = new java.util.ArrayList<>();
        commands.add("§e/team create §7- Tạo team mới");
        commands.add("§e/team invite <người chơi> §7- Mời người chơi vào team");
        commands.add("§e/team accept §7- Chấp nhận lời mời");
        commands.add("§e/team leave §7- Rời khỏi team");
        commands.add("§e/team kick <người chơi> §7- Đuổi thành viên");
        commands.add("§e/team disband §7- Giải tán team");
        commands.add("§e/team info §7- Xem thông tin team");
        commands.add("§e/team list §7- Xem danh sách teams");
        commands.add("§e/team transfer <người chơi> §7- Gửi yêu cầu chuyển quyền sở hữu (leader)");
        commands.add("§e/team taccept §7- Chấp nhận yêu cầu chuyển quyền sở hữu");
        commands.add("§e/team tdeny §7- Từ chối yêu cầu chuyển quyền sở hữu");
        commands.add("§e/team chat <tin nhắn> §7- Chat trong team");

        int commandsPerPage = 6;
        int totalPages = (int) Math.ceil((double) commands.size() / commandsPerPage);
        
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        player.sendMessage("§6=== Team Commands §7(Trang " + (page + 1) + "/" + totalPages + ") ===");
        
        int startIndex = page * commandsPerPage;
        int endIndex = Math.min(startIndex + commandsPerPage, commands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(commands.get(i));
        }
        
        if (totalPages > 1) {
            if (page < totalPages - 1) {
                player.sendMessage("§7Dùng §e/team help " + (page + 2) + " §7để xem trang tiếp theo.");
            }
            if (page > 0) {
                player.sendMessage("§7Dùng §e/team help " + page + " §7để xem trang trước.");
            }
        }
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/team chat <tin nhắn>");
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        chatManager.sendTeamMessage(player, message.toString().trim());
    }
}

