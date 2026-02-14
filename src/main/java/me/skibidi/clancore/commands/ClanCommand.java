package me.skibidi.clancore.commands;

import me.skibidi.clancore.chat.ClanChatManager;
import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.gui.ClanInfoGUI;
import me.skibidi.clancore.gui.ClanListGUI;
import me.skibidi.clancore.gui.ClanUpgradeGUI;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final WarManager warManager;
    private final EspManager espManager;
    private final ClanChatManager chatManager;
    private final ConfigManager configManager;
    private final ClanPointManager pointManager;
    private final BuffManager buffManager;

    public ClanCommand(ClanManager clanManager, WarManager warManager, EspManager espManager, 
                      ClanChatManager chatManager, ConfigManager configManager, ClanPointManager pointManager, BuffManager buffManager) {
        this.clanManager = clanManager;
        this.warManager = warManager;
        this.espManager = espManager;
        this.chatManager = chatManager;
        this.configManager = configManager;
        this.pointManager = pointManager;
        this.buffManager = buffManager;
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
            case "create", "c" -> handleCreate(player, args);
            case "join", "j" -> handleJoin(player, args);
            case "invite", "i" -> handleInvite(player, args);
            case "accept", "a" -> handleAccept(player);
            case "deny", "d" -> handleDeny(player);
            case "raccept", "ra" -> handleRequestAccept(player, args);
            case "rdeny", "rd" -> handleRequestDeny(player, args);
            case "requests", "req" -> handleRequests(player);
            case "leave", "l" -> handleLeave(player);
            case "kick", "k" -> handleKick(player, args);
            case "war", "w" -> handleWar(player, args);
            case "info", "in" -> handleInfo(player);
            case "upgrade", "up" -> handleUpgrade(player);
            case "list", "li" -> handleList(player);
            case "transfer", "t" -> handleTransfer(player, args);
            case "taccept", "ta" -> handleTransferAccept(player);
            case "tdeny", "td" -> handleTransferDeny(player);
            case "chat", "ch" -> handleChat(player, args);
            default -> {
                player.sendMessage("§cLệnh không hợp lệ. Gõ §e/clan §cđể xem danh sách lệnh.");
                player.sendMessage("§7Hoặc dùng §e/clan help <số trang> §7để xem các trang khác.");
            }
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan create <tên>");
            return;
        }

            String name = args[1];

            if (clanManager.createClan(name, player)) {
            player.sendMessage("§aĐã tạo clan §e" + name + "§a thành công!");
            espManager.updateFor(player);
        } else {
            player.sendMessage("§cKhông thể tạo clan. Có thể bạn đã có clan hoặc tên đã được sử dụng.");
        }
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan join <tên>");
            return;
        }

        Clan clan = clanManager.getClan(args[1]);
        if (clan == null) {
            player.sendMessage("§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        if (clanManager.requestJoin(player, clan)) {
            player.sendMessage("§aĐã gửi yêu cầu tham gia clan §e" + clan.getName() + "§a.");
        } else {
            player.sendMessage("§cKhông thể gửi yêu cầu tham gia.");
        }
    }

    private void handleInvite(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể invite
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể mời thành viên.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan invite <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy người chơi §e" + args[1] + "§c.");
            return;
        }

        if (clanManager.invitePlayer(clan, target)) {
            player.sendMessage("§aĐã mời §e" + target.getName() + " §avào clan.");
            target.sendMessage("§6Bạn đã được mời tham gia clan §e" + clan.getName() + "§6. Dùng §e/clan accept §6hoặc §e/clan deny§6.");
            // Phát sound ping để thông báo
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            player.sendMessage("§cKhông thể mời người chơi này.");
        }
    }

    private void handleAccept(Player player) {
        if (clanManager.acceptInvite(player)) {
            player.sendMessage("§aBạn đã tham gia clan thành công!");
            espManager.updateFor(player);
            // Apply buffs khi player join clan
            if (buffManager != null) {
                buffManager.applyBuffs(player);
            }
        } else {
            player.sendMessage("§cBạn không có lời mời nào đang chờ xử lý.");
        }
    }

    private void handleDeny(Player player) {
        if (clanManager.denyInvite(player)) {
            player.sendMessage("§cĐã từ chối lời mời.");
        } else {
            player.sendMessage("§cBạn không có lời mời nào đang chờ xử lý.");
        }
    }

    private void handleRequestAccept(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể accept request
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể chấp nhận yêu cầu tham gia.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan raccept <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cNgười chơi phải online để chấp nhận.");
            return;
        }

        UUID uuid = target.getUniqueId();
        if (clanManager.acceptRequest(clan, uuid)) {
            player.sendMessage("§aĐã chấp nhận yêu cầu tham gia từ §e" + target.getName() + "§a.");
            target.sendMessage("§aYêu cầu tham gia clan §e" + clan.getName() + " §ađã được chấp nhận.");
            espManager.updateFor(target);
            // Apply buffs khi player join clan
            if (buffManager != null) {
                buffManager.applyBuffs(target);
            }
        } else {
            player.sendMessage("§cKhông có yêu cầu tham gia từ người chơi này.");
        }
    }

    private void handleRequestDeny(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể deny request
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể từ chối yêu cầu tham gia.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan rdeny <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cNgười chơi phải online để từ chối.");
            return;
        }

        if (clanManager.denyRequest(clan, target.getUniqueId())) {
            player.sendMessage("§cĐã từ chối yêu cầu tham gia từ §e" + target.getName() + "§c.");
            target.sendMessage("§cYêu cầu tham gia clan §e" + clan.getName() + " §cđã bị từ chối.");
        } else {
            player.sendMessage("§cKhông có yêu cầu tham gia từ người chơi này.");
        }
    }

    private void handleRequests(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể xem requests
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể xem yêu cầu tham gia.");
            return;
        }

        if (clan.getJoinRequests().isEmpty()) {
            player.sendMessage("§7Không có yêu cầu tham gia nào.");
            return;
        }

        player.sendMessage("§6=== Yêu cầu tham gia ===");
        for (UUID uuid : clan.getJoinRequests().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            player.sendMessage("§7- §e" + (p != null ? p.getName() : uuid));
        }
    }

    private void handleLeave(Player player) {
        if (!clanManager.isInClan(player)) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        if (clanManager.leaveClan(player)) {
            espManager.clear(player);
            if (buffManager != null) {
                buffManager.removeBuffs(player);
            }
            player.sendMessage("§cBạn đã rời khỏi clan.");
        }
        // Nếu leaveClan() trả về false, thông báo lỗi đã được gửi trong leaveClan()
    }

    private void handleKick(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể kick
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể đuổi thành viên.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan kick <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage("§cNgười chơi phải online để đuổi.");
            return;
        }

        if (clanManager.kickMember(clan, target.getUniqueId())) {
            player.sendMessage("§aĐã đuổi §e" + target.getName() + " §akhỏi clan.");
            target.sendMessage("§cBạn đã bị đuổi khỏi clan §e" + clan.getName() + "§c.");
            espManager.clear(target);
            if (buffManager != null) {
                buffManager.removeBuffs(target);
            }
        } else {
            player.sendMessage("§cKhông thể đuổi người chơi này.");
        }
    }

    private void handleWar(Player player, String[] args) {
        Clan own = clanManager.getClan(player);
        if (own == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner của clan này mới có thể khai chiến
        if (!clanManager.isOwner(player, own)) {
            player.sendMessage("§cChỉ chủ clan mới có thể khai chiến.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan war <clan>");
            return;
        }

        Clan targetClan = clanManager.getClan(args[1]);
        if (targetClan == null) {
            player.sendMessage("§cKhông tìm thấy clan §e" + args[1] + "§c.");
            return;
        }

        if (warManager.startWar(own, targetClan) != null) {
            player.sendMessage("§c§lBạn đã khai chiến với clan §e§l" + targetClan.getName() + "§c§l!");
            espManager.updateAll();
        } else {
            player.sendMessage("§cBạn đã ở trong trạng thái chiến tranh với clan này hoặc mục tiêu không hợp lệ.");
        }
    }

    private void handleInfo(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào!");
            return;
        }
        ClanInfoGUI.open(player, clan, 0);
    }

    private void handleUpgrade(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào!");
            return;
        }
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể nâng cấp!");
            return;
        }
        ClanUpgradeGUI.open(player, clan, configManager, pointManager);
    }

    private void handleList(Player player) {
        // Mở GUI thay vì hiển thị trong chat
        ClanListGUI.open(player, clanManager, 0);
    }

    private void handleTransfer(Player player, String[] args) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }
        // Kiểm tra phân quyền: chỉ owner mới có thể transfer
        if (!clanManager.isOwner(player, clan)) {
            player.sendMessage("§cChỉ chủ clan mới có thể chuyển quyền sở hữu.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan transfer <người chơi>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cNgười chơi §e" + args[1] + " §ckhông online hoặc không tồn tại.");
            return;
        }

        if (!clan.hasMember(target.getUniqueId())) {
            player.sendMessage("§cNgười chơi này không phải thành viên của clan.");
            return;
        }

        if (clanManager.requestTransferOwnership(clan, target.getUniqueId())) {
            player.sendMessage("§aĐã gửi yêu cầu chuyển quyền sở hữu clan cho §e" + target.getName() + "§a!");
            target.sendMessage("§6" + player.getName() + " §eđã gửi yêu cầu chuyển quyền sở hữu clan §6" + clan.getName() + " §echo bạn!");
            target.sendMessage("§7Dùng §e/clan taccept §7để chấp nhận hoặc §e/clan tdeny §7để từ chối.");
            // Phát sound ping để thông báo
            target.playSound(target.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        } else {
            player.sendMessage("§cKhông thể gửi yêu cầu chuyển quyền sở hữu. Vui lòng thử lại sau.");
        }
    }

    private void handleTransferAccept(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }

        UUID pendingTransfer = clan.getPendingTransferTo();
        if (pendingTransfer == null || !pendingTransfer.equals(player.getUniqueId())) {
            player.sendMessage("§cBạn không có yêu cầu chuyển quyền sở hữu nào.");
            return;
        }

        // Lưu UUID của old owner trước khi acceptTransferOwnership() thay đổi owner
        UUID oldOwnerUuid = clan.getOwner();
        Player oldOwner = Bukkit.getPlayer(oldOwnerUuid);
        
        if (clanManager.acceptTransferOwnership(clan)) {
            player.sendMessage("§aBạn đã trở thành chủ clan mới của §e" + clan.getName() + "§a!");
            if (oldOwner != null && oldOwner.isOnline()) {
                oldOwner.sendMessage("§a" + player.getName() + " §eđã chấp nhận yêu cầu chuyển quyền sở hữu clan!");
            }
            
            // Thông báo cho tất cả members (loại trừ old owner và new owner)
            // Create a copy to avoid ConcurrentModificationException
            for (UUID memberUuid : new java.util.HashSet<>(clan.getMembers())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline() && !memberUuid.equals(oldOwnerUuid) && !memberUuid.equals(player.getUniqueId())) {
                    member.sendMessage("§6" + player.getName() + " §eđã trở thành chủ clan mới!");
                }
            }
        } else {
            player.sendMessage("§cKhông thể chuyển quyền sở hữu. Vui lòng thử lại sau.");
        }
    }

    private void handleTransferDeny(Player player) {
        Clan clan = clanManager.getClan(player);
        if (clan == null) {
            player.sendMessage("§cBạn không ở trong clan nào.");
            return;
        }

        UUID pendingTransfer = clan.getPendingTransferTo();
        if (pendingTransfer == null) {
            player.sendMessage("§cBạn không có yêu cầu chuyển quyền sở hữu nào.");
            return;
        }

        UUID playerUuid = player.getUniqueId();
        boolean isRecipient = pendingTransfer.equals(playerUuid);
        boolean isOwner = clan.getOwner().equals(playerUuid);

        // Allow both recipient and owner to cancel the transfer
        if (!isRecipient && !isOwner) {
            player.sendMessage("§cBạn không có quyền hủy yêu cầu chuyển quyền sở hữu này.");
            return;
        }

        // Cancel the transfer
        clanManager.cancelTransferOwnership(clan);
        
        if (isRecipient) {
            // Recipient denied
            player.sendMessage("§cBạn đã từ chối yêu cầu chuyển quyền sở hữu clan.");
            Player oldOwner = Bukkit.getPlayer(clan.getOwner());
            if (oldOwner != null && oldOwner.isOnline()) {
                oldOwner.sendMessage("§c" + player.getName() + " §eđã từ chối yêu cầu chuyển quyền sở hữu clan.");
            }
        } else if (isOwner) {
            // Owner cancelled their own request
            player.sendMessage("§cBạn đã hủy yêu cầu chuyển quyền sở hữu clan.");
            Player recipient = Bukkit.getPlayer(pendingTransfer);
            if (recipient != null && recipient.isOnline()) {
                recipient.sendMessage("§c" + player.getName() + " §eđã hủy yêu cầu chuyển quyền sở hữu clan.");
            }
        }
    }

    private void showHelpPage(Player player, int page) {
        java.util.List<String> commands = new java.util.ArrayList<>();
        commands.add("§e/clan create <tên> §7- Tạo clan mới");
        commands.add("§e/clan join <tên> §7- Gửi yêu cầu tham gia clan");
        commands.add("§e/clan invite <người chơi> §7- Mời người chơi vào clan");
        commands.add("§e/clan accept §7- Chấp nhận lời mời");
        commands.add("§e/clan deny §7- Từ chối lời mời");
        commands.add("§e/clan raccept <người chơi> §7- Chấp nhận yêu cầu tham gia");
        commands.add("§e/clan rdeny <người chơi> §7- Từ chối yêu cầu tham gia");
        commands.add("§e/clan requests §7- Xem danh sách yêu cầu");
        commands.add("§e/clan leave §7- Rời khỏi clan");
        commands.add("§e/clan kick <người chơi> §7- Đuổi thành viên");
        commands.add("§e/clan war <clan> §7- Khai chiến với clan khác");
        commands.add("§e/clan info §7- Xem thông tin clan");
        commands.add("§e/clan upgrade §7- Nâng cấp clan (chủ clan)");
        commands.add("§e/clan list §7- Xem danh sách clans");
        commands.add("§e/clan transfer <người chơi> §7- Chuyển quyền sở hữu (chủ clan)");
        commands.add("§e/clan chat <tin nhắn> §7- Chat trong clan");

        int commandsPerPage = 6;
        int totalPages = (int) Math.ceil((double) commands.size() / commandsPerPage);
        
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        player.sendMessage("§6=== Clan Commands §7(Trang " + (page + 1) + "/" + totalPages + ") ===");
        
        int startIndex = page * commandsPerPage;
        int endIndex = Math.min(startIndex + commandsPerPage, commands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            player.sendMessage(commands.get(i));
        }
        
        if (totalPages > 1) {
            if (page < totalPages - 1) {
                player.sendMessage("§7Dùng §e/clan help " + (page + 2) + " §7để xem trang tiếp theo.");
            }
            if (page > 0) {
                player.sendMessage("§7Dùng §e/clan help " + page + " §7để xem trang trước.");
            }
        }
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCú pháp: §e/clan chat <tin nhắn>");
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        chatManager.sendClanMessage(player, message.toString().trim());
    }
}

