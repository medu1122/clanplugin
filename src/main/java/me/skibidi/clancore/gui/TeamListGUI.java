package me.skibidi.clancore.gui;

import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.team.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class TeamListGUI {

    private static final int TEAMS_PER_PAGE = 28; // 7 rows x 4 columns
    private static final int[] TEAM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public static void open(Player player, TeamManager teamManager, int page) {
        Collection<Team> allTeams = teamManager.getAllTeams();
        
        // Lọc chỉ các team có ít nhất 1 member online
        List<Team> activeTeams = new ArrayList<>();
        for (Team team : allTeams) {
            boolean hasOnlineMember = false;
            for (UUID memberUuid : new HashSet<>(team.getMembers())) {
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

        int totalPages = (int) Math.ceil((double) activeTeams.size() / TEAMS_PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, "§bDanh Sách Teams §7(Trang " + (page + 1) + "/" + Math.max(1, totalPages) + ")");

        // Info item ở slot 4
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§bThông Tin");
            List<String> lore = new ArrayList<>();
            lore.add("§7Tổng số teams: §b" + activeTeams.size());
            lore.add("§7Click vào team để xem chi tiết");
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inv.setItem(4, infoItem);

        // Team items
        int startIndex = page * TEAMS_PER_PAGE;
        int endIndex = Math.min(startIndex + TEAMS_PER_PAGE, activeTeams.size());

        for (int i = startIndex; i < endIndex; i++) {
            Team team = activeTeams.get(i);
            int slot = TEAM_SLOTS[i - startIndex];

            // Đếm số member online
            int onlineCount = 0;
            List<String> onlineMembers = new ArrayList<>();
            
            for (UUID memberUuid : new HashSet<>(team.getMembers())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    onlineCount++;
                    onlineMembers.add(member.getName());
                }
            }

            // Lấy leader info
            OfflinePlayer leader = Bukkit.getOfflinePlayer(team.getLeader());
            String leaderName = leader.getName();
            if (leaderName == null) {
                leaderName = "Unknown (" + team.getLeader().toString().substring(0, 8) + "...)";
            }
            boolean leaderOnline = leader.isOnline();

            ItemStack teamItem = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) teamItem.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setDisplayName("§bTeam: §e" + leaderName);
                List<String> lore = new ArrayList<>();
                lore.add("§7Leader: " + (leaderOnline ? "§a" : "§c") + leaderName);
                lore.add("§7  " + (leaderOnline ? "§a● Online" : "§c● Offline"));
                lore.add("§7Members online: §b" + onlineCount + "/" + team.getMembers().size());
                if (!onlineMembers.isEmpty()) {
                    lore.add("§7Online: §b" + String.join("§7, §b", onlineMembers));
                }
                lore.add("");
                lore.add("§eClick để xem chi tiết!");
                skullMeta.setLore(lore);
                skullMeta.setOwningPlayer(leader);
                teamItem.setItemMeta(skullMeta);
            }
            inv.setItem(slot, teamItem);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backMeta = back.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Trang Trước");
                back.setItemMeta(backMeta);
            }
            inv.setItem(45, back);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName("§7Trang Sau");
                next.setItemMeta(nextMeta);
            }
            inv.setItem(53, next);
        }

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§cĐóng");
            close.setItemMeta(closeMeta);
        }
        inv.setItem(49, close);

        player.openInventory(inv);
    }
}
