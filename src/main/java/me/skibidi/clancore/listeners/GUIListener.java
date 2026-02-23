package me.skibidi.clancore.listeners;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.gui.ClanInfoGUI;
import me.skibidi.clancore.gui.ClanListGUI;
import me.skibidi.clancore.gui.ClanUpgradeGUI;
import me.skibidi.clancore.gui.ClanWarGUI;
import me.skibidi.clancore.gui.SellItemsGUI;
import me.skibidi.clancore.gui.TeamInfoGUI;
import me.skibidi.clancore.gui.TeamListGUI;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.war.WarManager;
import me.skibidi.clancore.team.model.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final ClanManager clanManager;
    private final TeamManager teamManager;
    private final ClanPointManager pointManager;
    private final ConfigManager configManager;
    private final WarManager warManager;
    private final Plugin plugin;
    private final Map<UUID, Integer> clanInfoPages = new HashMap<>();
    private final Map<UUID, Integer> teamInfoPages = new HashMap<>();
    private final Map<UUID, Integer> clanListPages = new HashMap<>();
    private final Map<UUID, Integer> teamListPages = new HashMap<>();
    private final Map<UUID, Integer> clanWarPages = new HashMap<>();

    public GUIListener(ClanManager clanManager, TeamManager teamManager, ClanPointManager pointManager, ConfigManager configManager, WarManager warManager, Plugin plugin) {
        this.clanManager = clanManager;
        this.teamManager = teamManager;
        this.pointManager = pointManager;
        this.configManager = configManager;
        this.warManager = warManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();

        // Clan Info GUI - cancel all clicks including empty slots
        if (title.startsWith("§6Clan: §e")) {
            event.setCancelled(true);
            
            // Early return if clicking empty slot (no action needed)
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Close button check (outside ARROW check since it uses BARRIER)
            if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (clicked.getType() == Material.ARROW) {
                Clan clan = clanManager.getClan(player);
                if (clan == null) {
                    player.closeInventory();
                    return;
                }

                int currentPage = clanInfoPages.getOrDefault(player.getUniqueId(), 0);
                if (event.getSlot() == 45) { // Back
                    if (currentPage > 0) {
                        clanInfoPages.put(player.getUniqueId(), currentPage - 1);
                        ClanInfoGUI.open(player, clan, currentPage - 1);
                    }
                } else if (event.getSlot() == 53) { // Next
                    int totalPages = (int) Math.ceil((double) clan.getMembers().size() / 28.0);
                    if (currentPage < totalPages - 1) {
                        clanInfoPages.put(player.getUniqueId(), currentPage + 1);
                        ClanInfoGUI.open(player, clan, currentPage + 1);
                    }
                }
            }
        }

        // Team Info GUI - cancel all clicks including empty slots
        if (title.startsWith("§bTeam Info §7")) {
            event.setCancelled(true);
            
            // Early return if clicking empty slot (no action needed)
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Close button check (outside ARROW check since it uses BARRIER)
            if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            if (clicked.getType() == Material.ARROW) {
                Team team = teamManager.getTeam(player);
                if (team == null) {
                    player.closeInventory();
                    return;
                }

                int currentPage = teamInfoPages.getOrDefault(player.getUniqueId(), 0);
                if (event.getSlot() == 45) { // Back
                    if (currentPage > 0) {
                        teamInfoPages.put(player.getUniqueId(), currentPage - 1);
                        TeamInfoGUI.open(player, team, currentPage - 1);
                    }
                } else if (event.getSlot() == 53) { // Next
                    int totalPages = (int) Math.ceil((double) team.getMembers().size() / 28.0);
                    if (currentPage < totalPages - 1) {
                        teamInfoPages.put(player.getUniqueId(), currentPage + 1);
                        TeamInfoGUI.open(player, team, currentPage + 1);
                    }
                }
            }
        }

        // Clan Upgrade GUI - cancel all clicks including empty slots
        if (title.equals("§6Nâng Cấp Clan")) {
            event.setCancelled(true);
            
            Clan clan = clanManager.getClan(player);
            if (clan == null) {
                player.closeInventory();
                return;
            }
            
            // Early return if clicking empty slot (no action needed)
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (event.getSlot() == 22) { // Ô Đá quý (sắp ra mắt) - không mở gì
                return;
            }

            if (event.getSlot() == 31) { // Upgrade button
                event.setCancelled(true);
                if (pointManager.upgradeClan(player)) {
                    // Refresh GUI
                    ClanUpgradeGUI.open(player, clan, configManager, pointManager);
                }
                return;
            }

            if (event.getSlot() == 49) { // Close
                event.setCancelled(true);
                player.closeInventory();
                return;
            }

            // Block other slots
            event.setCancelled(true);
        }

        // Clan War Manager GUI
        if (title.startsWith("§cQuản Lý Chiến Tranh §7")) {
            event.setCancelled(true);
            Clan clan = clanManager.getClan(player);
            if (clan == null) { player.closeInventory(); return; }
            if (clicked == null || clicked.getType() == Material.AIR) return;

            int page = clanWarPages.getOrDefault(player.getUniqueId(), 0);
            java.util.List<Clan> others = new java.util.ArrayList<>();
            for (Clan c : clanManager.getAllClans()) {
                if (c != clan) others.add(c);
            }

            if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == 45 && clicked.getType() == Material.ARROW && page > 0) {
                clanWarPages.put(player.getUniqueId(), page - 1);
                ClanWarGUI.open(player, clan, clanManager, warManager, page - 1);
                return;
            }
            if (event.getSlot() == 53 && clicked.getType() == Material.ARROW) {
                int totalPages = Math.max(1, (int) Math.ceil((double) others.size() / 28));
                if (page < totalPages - 1) {
                    clanWarPages.put(player.getUniqueId(), page + 1);
                    ClanWarGUI.open(player, clan, clanManager, warManager, page + 1);
                }
                return;
            }
            int[] slots = ClanWarGUI.getClanSlots();
            for (int i = 0; i < slots.length; i++) {
                if (event.getSlot() == slots[i]) {
                    int idx = page * 28 + i;
                    if (idx >= others.size()) break;
                    Clan target = others.get(idx);
                    boolean was = warManager.isWarEnabled(clan, target);
                    warManager.setWarEnabled(clan, target, !was);
                    player.sendMessage(was ? "§aĐã tắt chiến tranh với clan §e" + target.getName() + "§a." : "§cĐã bật chiến tranh với clan §e" + target.getName() + "§c. (Chỉ khi họ cũng bật mới là đang chiến tranh)");
                    ClanWarGUI.open(player, clan, clanManager, warManager, page);
                    break;
                }
            }
            return;
        }

        // Sell Items GUI - allow item placement in sell slots
        if (title.equals("§6Bán Vật Phẩm")) {
            Clan clan = clanManager.getClan(player);
            if (clan == null) {
                event.setCancelled(true);
                player.closeInventory();
                return;
            }

            // Allow placing items in sell slots (don't cancel, allow normal interaction)
            if (SellItemsGUI.isSellSlot(event.getSlot())) {
                // Allow normal inventory interaction
                // Update info panel when items change
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (player.getOpenInventory().getTitle().equals("§6Bán Vật Phẩm")) {
                                SellItemsGUI.updateInfoPanel(
                                        player.getOpenInventory().getTopInventory(),
                                        clan,
                                        configManager,
                                        pointManager
                                );
                            }
                        },
                        1L
                );
                return; // Don't cancel - allow item placement
            }
            
            // For non-sell slots, cancel clicks (including empty slots)
            event.setCancelled(true);
            
            // Early return if clicking empty slot in non-sell slots
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (event.getSlot() == 40) { // Sell all - đã chuyển sang Đá quý (sắp ra mắt)
                event.setCancelled(true);
                player.sendMessage("§eBán vật phẩm đổi Đá quý đang được cập nhật.");
                return;
            }

            if (event.getSlot() == 45) { // Back button
                event.setCancelled(true);
                ClanUpgradeGUI.open(player, clan, configManager, pointManager);
                return;
            }

            if (event.getSlot() == 49) { // Close
                event.setCancelled(true);
                player.closeInventory();
                return;
            }

            // Block other slots
            event.setCancelled(true);
        }

        // Clan List GUI
        if (title.startsWith("§6Danh Sách Clans §7")) {
            event.setCancelled(true);
            
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Close button
            if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            // Pagination
            if (clicked.getType() == Material.ARROW) {
                int currentPage = clanListPages.getOrDefault(player.getUniqueId(), 0);
                if (event.getSlot() == 45) { // Back
                    if (currentPage > 0) {
                        clanListPages.put(player.getUniqueId(), currentPage - 1);
                        ClanListGUI.open(player, clanManager, currentPage - 1);
                    }
                } else if (event.getSlot() == 53) { // Next
                    int totalClans = clanManager.getAllClans().size();
                    int totalPages = (int) Math.ceil((double) totalClans / 28.0);
                    if (currentPage < totalPages - 1) {
                        clanListPages.put(player.getUniqueId(), currentPage + 1);
                        ClanListGUI.open(player, clanManager, currentPage + 1);
                    }
                }
                return;
            }

            // Click vào clan để xem chi tiết
            if (clicked.getType().name().contains("BANNER") && clicked.hasItemMeta()) {
                String clanName = clicked.getItemMeta().getDisplayName().replace("§6", "");
                Clan clan = clanManager.getClan(clanName);
                if (clan != null) {
                    ClanInfoGUI.open(player, clan, 0);
                }
            }
        }

        // Team List GUI
        if (title.startsWith("§bDanh Sách Teams §7")) {
            event.setCancelled(true);
            
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Close button
            if (event.getSlot() == 49 && clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }

            // Pagination
            if (clicked.getType() == Material.ARROW) {
                int currentPage = teamListPages.getOrDefault(player.getUniqueId(), 0);
                if (event.getSlot() == 45) { // Back
                    if (currentPage > 0) {
                        teamListPages.put(player.getUniqueId(), currentPage - 1);
                        TeamListGUI.open(player, teamManager, currentPage - 1);
                    }
                } else if (event.getSlot() == 53) { // Next
                    // Tính tổng số active teams
                    java.util.Collection<Team> allTeams = teamManager.getAllTeams();
                    int activeCount = 0;
                    for (Team team : allTeams) {
                        boolean hasOnline = false;
                        for (UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
                            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
                            if (member != null && member.isOnline()) {
                                hasOnline = true;
                                break;
                            }
                        }
                        if (hasOnline) activeCount++;
                    }
                    int totalPages = (int) Math.ceil((double) activeCount / 28.0);
                    if (currentPage < totalPages - 1) {
                        teamListPages.put(player.getUniqueId(), currentPage + 1);
                        TeamListGUI.open(player, teamManager, currentPage + 1);
                    }
                }
                return;
            }

            // Click vào team để xem chi tiết
            if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
                // Lấy leader name từ display name
                String displayName = clicked.getItemMeta().getDisplayName();
                if (displayName.contains("Team: §e")) {
                    String leaderName = displayName.replace("§bTeam: §e", "");
                    // Tìm team theo leader name
                    for (Team team : teamManager.getAllTeams()) {
                        org.bukkit.OfflinePlayer leader = org.bukkit.Bukkit.getOfflinePlayer(team.getLeader());
                        if (leader.getName() != null && leader.getName().equals(leaderName)) {
                            TeamInfoGUI.open(player, team, 0);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            UUID uuid = player.getUniqueId();
            clanInfoPages.remove(uuid);
            teamInfoPages.remove(uuid);
            clanListPages.remove(uuid);
            teamListPages.remove(uuid);
            clanWarPages.remove(uuid);
        }
    }
}
