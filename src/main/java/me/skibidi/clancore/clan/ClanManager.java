package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.storage.repository.ClanMemberRepository;
import me.skibidi.clancore.storage.repository.ClanRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class ClanManager {

    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, Clan> playerClan = new HashMap<>();
    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private ConfigManager configManager;

    public ClanManager(ClanRepository clanRepository, ClanMemberRepository memberRepository) {
        this.clanRepository = clanRepository;
        this.memberRepository = memberRepository;
        // Note: loadAllClansFromDB() will be called after setConfigManager() is called
    }

    /**
     * Set ConfigManager (called after initialization to avoid circular dependency).
     */
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadAllClansFromDB();
    }

    /**
     * Load tất cả clans từ DB khi khởi động plugin.
     */
    private void loadAllClansFromDB() {
        try {
            List<Clan> dbClans = clanRepository.loadAllClans();
            for (Clan clan : dbClans) {
                // Set maxSlots based on clan level from config (Bug 1 fix)
                if (configManager != null) {
                    ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(clan.getLevel());
                    if (levelConfig != null) {
                        clan.setMaxSlots(levelConfig.getMaxMembers());
                    }
                }

                String key = clan.getName().toLowerCase(Locale.ROOT);
                
                // Load members BEFORE adding to maps to avoid partial state if loading fails
                try {
                    List<UUID> members = memberRepository.loadMembers(clan.getName());
                    for (UUID uuid : members) {
                        // Chỉ add nếu chưa có (tránh duplicate owner)
                        if (!clan.getMembers().contains(uuid)) {
                            clan.getMembers().add(uuid);
                        }
                    }
                    
                    // Only add to maps after successfully loading all members
                    clans.put(key, clan);
                    
                    // Ensure owner is always added to playerClan map (Bug fix: owner might be missing from clan_members table)
                    UUID ownerUuid = clan.getOwner();
                    playerClan.put(ownerUuid, clan);
                    
                    // Add all members to playerClan map
                    for (UUID uuid : members) {
                        playerClan.put(uuid, clan);
                    }
                } catch (SQLException memberLoadEx) {
                    // If loading members fails, skip this clan to avoid partial state
                    memberLoadEx.printStackTrace();
                    System.err.println("[ClanCore] ERROR: Failed to load members for clan " + clan.getName() + 
                            ". Skipping clan to avoid partial state.");
                    // Don't add clan to maps - it will be skipped
                    continue;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createClan(String name, Player owner) {
        if (name == null || name.isBlank()) return false;

        String key = name.toLowerCase(Locale.ROOT);
        if (clans.containsKey(key)) return false;
        if (playerClan.containsKey(owner.getUniqueId())) return false;

        Clan clan = new Clan(name, owner.getUniqueId());
        
        // Set initial maxSlots based on level 1 config
        if (configManager != null) {
            ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(1);
            if (levelConfig != null) {
                clan.setMaxSlots(levelConfig.getMaxMembers());
            }
        }

        // Save to DB first, only add to memory if successful
        try {
            clanRepository.saveClan(clan);
            try {
                memberRepository.addMember(clan.getName(), owner.getUniqueId(), "OWNER");
                // Only add to memory after both DB operations succeed
                clans.put(key, clan);
                playerClan.put(owner.getUniqueId(), clan);
                return true;
            } catch (SQLException e) {
                // Rollback: delete clan from DB if member add fails (Bug 2 fix)
                try {
                    clanRepository.deleteClan(clan.getName());
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
                e.printStackTrace();
                owner.sendMessage("§cLỗi khi tạo clan! Vui lòng thử lại sau.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            owner.sendMessage("§cLỗi khi tạo clan! Vui lòng thử lại sau.");
            return false;
        }
    }

    public Clan getClan(String name) {
        if (name == null) return null;
        return clans.get(name.toLowerCase(Locale.ROOT));
    }

    public Clan getClan(Player player) {
        return playerClan.get(player.getUniqueId());
    }

    public boolean isInClan(Player player) {
        return getClan(player) != null;
    }

    public Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clans.values());
    }

    public void joinClan(Player player, Clan clan) {
        if (clan == null) return;
        UUID uuid = player.getUniqueId();
        
        // Early check to avoid unnecessary DB operations, but don't rely on it for race condition prevention
        if (clan.isFull()) return;

        // Save to DB first, only update memory if successful
        try {
            memberRepository.addMember(clan.getName(), uuid, "MEMBER");
            // Only update memory after DB save succeeds
            clan.addMember(uuid);
            
            // Check capacity AFTER adding to handle concurrent requests
            // If capacity exceeded due to race condition, rollback
            if (clan.getMembers().size() > clan.getMaxSlots()) {
                // Exceeded capacity - rollback DB first, then memory
                try {
                    memberRepository.removeMember(clan.getName(), uuid);
                    // DB rollback succeeded - now remove from memory
                    clan.getMembers().remove(uuid);
                    player.sendMessage("§cClan đã đầy! Không thể tham gia.");
                } catch (SQLException rollbackEx) {
                    // DB rollback failed - keep memory consistent with DB
                    // Add to playerClan to maintain consistency (member is in both DB and memory)
                    // The clan will temporarily be over capacity, but data will be consistent
                    rollbackEx.printStackTrace();
                    System.err.println("[ClanCore] CRITICAL: Failed to rollback member " + uuid + 
                            " from clan " + clan.getName() + " in database. " +
                            "Clan is temporarily over capacity. Please investigate.");
                    // Add to playerClan to maintain consistency with DB and memory
                    playerClan.put(uuid, clan);
                    player.sendMessage("§e[LƯU Ý] §aĐã tham gia clan §e" + clan.getName() + "§a, nhưng clan đã vượt quá giới hạn thành viên do lỗi hệ thống. Vui lòng liên hệ admin!");
                }
                return;
            }
            
            // Success - update tracking structures
            playerClan.put(uuid, clan);
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cLỗi khi tham gia clan! Vui lòng thử lại sau.");
        }
    }

    /**
     * Rời khỏi clan.
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean leaveClan(Player player) {
        Clan clan = playerClan.get(player.getUniqueId());
        if (clan == null) return false;

        // Prevent owner from leaving - must transfer ownership first
        if (isOwner(player, clan)) {
            player.sendMessage("§cChủ clan không thể rời clan! Hãy chuyển quyền sở hữu cho thành viên khác trước.");
            return false;
        }

        // Remove from DB first, only update memory if successful
        try {
            memberRepository.removeMember(clan.getName(), player.getUniqueId());
            // Only update memory after DB save succeeds
            clan.removeMember(player.getUniqueId());
            playerClan.remove(player.getUniqueId());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("§cLỗi khi rời clan! Vui lòng thử lại sau.");
            return false;
        }
    }

    public boolean sameClan(Player a, Player b) {
        Clan ca = getClan(a);
        Clan cb = getClan(b);
        return ca != null && ca == cb;
    }

    // ========== INVITES ==========

    public boolean invitePlayer(Clan clan, Player target) {
        if (clan == null) return false;
        if (playerClan.containsKey(target.getUniqueId())) return false;

        // Store timestamp for 30-second expiration
        clan.getInvitedPlayers().put(target.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    /**
     * Player chấp nhận invite vào clan (nếu có).
     */
    public boolean acceptInvite(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerClan.containsKey(uuid)) return false;

        // Try all clans that invited the player, not just the first one
        for (Clan clan : clans.values()) {
            // Check if player was invited and invite is still valid (not expired)
            if (!clan.getInvitedPlayers().containsKey(uuid)) {
                continue;
            }
            
            // Check expiration (30 seconds)
            if (!clan.isInviteValid(uuid)) {
                // Invite expired, remove it
                clan.getInvitedPlayers().remove(uuid);
                continue;
            }
            
            // Check capacity BEFORE attempting to join (prevent late acceptances causing over-capacity)
            if (!clan.canAddMember()) {
                // Clan is full, remove expired invite and try next
                clan.getInvitedPlayers().remove(uuid);
                continue;
            }
            
            // Save to DB first, only update memory if successful
            try {
                memberRepository.addMember(clan.getName(), uuid, "MEMBER");
                
                // Check capacity RIGHT BEFORE adding to memory (final safety check)
                // Note: Member is already in DB, so we check if adding to memory would exceed capacity
                // We check: current_members + 1 > maxSlots, which is equivalent to: current_members >= maxSlots
                if (clan.getMembers().size() >= clan.getMaxSlots()) {
                    // Capacity was exceeded between DB save and now - rollback
                    try {
                        memberRepository.removeMember(clan.getName(), uuid);
                        // DB rollback succeeded - remove invite and try next clan
                        clan.getInvitedPlayers().remove(uuid);
                    } catch (SQLException rollbackEx) {
                        // DB rollback failed - critical error
                        rollbackEx.printStackTrace();
                        System.err.println("[ClanCore] CRITICAL: Failed to rollback member " + uuid + 
                                " from clan " + clan.getName() + " in database. " +
                                "Clan may be over capacity. Please investigate.");
                        // Remove invite to prevent repeated attempts
                        clan.getInvitedPlayers().remove(uuid);
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendMessage("§cLỗi hệ thống khi tham gia clan! Vui lòng liên hệ admin.");
                        }
                    }
                    continue;
                }
                
                // Add to in-memory set (this automatically removes from invitedPlayers and joinRequests)
                clan.addMember(uuid);
                playerClan.put(uuid, clan);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage("§cLỗi khi tham gia clan! Vui lòng thử lại sau.");
                }
                // Remove invite to prevent repeated attempts on clan with DB errors
                clan.getInvitedPlayers().remove(uuid);
                // DB error for this clan, try the next one
                continue;
            }
        }
        return false;
    }

    public boolean denyInvite(Player player) {
        UUID uuid = player.getUniqueId();
        boolean removed = false;
        for (Clan clan : clans.values()) {
            removed |= (clan.getInvitedPlayers().remove(uuid) != null);
        }
        return removed;
    }

    // ========== JOIN REQUESTS ==========

    public boolean requestJoin(Player player, Clan clan) {
        if (clan == null) return false;
        if (playerClan.containsKey(player.getUniqueId())) return false;

        // Store timestamp for 30-second expiration
        clan.getJoinRequests().put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    public Set<UUID> getJoinRequests(Clan clan) {
        // Return only valid (non-expired) requests
        clan.cleanupExpiredRequests();
        return new HashSet<>(clan.getJoinRequests().keySet());
    }

    public boolean acceptRequest(Clan clan, UUID requester) {
        if (clan == null) return false;
        if (!clan.getJoinRequests().containsKey(requester)) return false;
        
        // Check expiration (30 seconds)
        if (!clan.isJoinRequestValid(requester)) {
            // Request expired, remove it
            clan.getJoinRequests().remove(requester);
            return false;
        }
        
        // Check capacity BEFORE attempting to join (prevent late acceptances causing over-capacity)
        if (!clan.canAddMember()) {
            // Clan is full, remove expired request
            clan.getJoinRequests().remove(requester);
            Player p = Bukkit.getPlayer(requester);
            if (p != null) {
                p.sendMessage("§cClan đã đầy! Yêu cầu tham gia đã bị hủy.");
            }
            return false;
        }

        // Save to DB first, only update memory if successful
        try {
            memberRepository.addMember(clan.getName(), requester, "MEMBER");
            
            // Check capacity RIGHT BEFORE adding to memory (final safety check)
            // Note: Member is already in DB, so we check if adding to memory would exceed capacity
            // We check: current_members + 1 > maxSlots, which is equivalent to: current_members >= maxSlots
            if (clan.getMembers().size() >= clan.getMaxSlots()) {
                // Capacity was exceeded between DB save and now - rollback
                try {
                    memberRepository.removeMember(clan.getName(), requester);
                    // DB rollback succeeded - remove request
                    clan.getJoinRequests().remove(requester);
                    Player p = Bukkit.getPlayer(requester);
                    if (p != null) {
                        p.sendMessage("§cClan đã đầy! Yêu cầu tham gia đã bị hủy.");
                    }
                } catch (SQLException rollbackEx) {
                    // DB rollback failed - critical error
                    rollbackEx.printStackTrace();
                    System.err.println("[ClanCore] CRITICAL: Failed to rollback member " + requester + 
                            " from clan " + clan.getName() + " in database. " +
                            "Clan may be over capacity. Please investigate.");
                    // Remove request to prevent repeated attempts
                    clan.getJoinRequests().remove(requester);
                    Player p = Bukkit.getPlayer(requester);
                    if (p != null) {
                        p.sendMessage("§cLỗi hệ thống khi tham gia clan! Vui lòng liên hệ admin.");
                    }
                }
                return false;
            }
            
            // Add to in-memory set (this automatically removes from invitedPlayers and joinRequests)
            clan.addMember(requester);
            playerClan.put(requester, clan);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Player p = Bukkit.getPlayer(requester);
            if (p != null) {
                p.sendMessage("§cLỗi khi tham gia clan! Vui lòng thử lại sau.");
            }
            // Remove request to prevent repeated attempts on clan with DB errors
            clan.getJoinRequests().remove(requester);
            return false;
        }
    }

    public boolean denyRequest(Clan clan, UUID requester) {
        if (clan == null) return false;
        return clan.getJoinRequests().remove(requester) != null;
    }

    // ========== MANAGEMENT ==========

    public boolean isOwner(Player player, Clan clan) {
        if (clan == null) return false;
        return clan.getOwner().equals(player.getUniqueId());
    }

    public boolean kickMember(Clan clan, UUID target) {
        if (clan == null) return false;
        if (clan.getOwner().equals(target)) return false; // không kick owner
        if (!clan.hasMember(target)) return false;

        // Remove from DB first, only update memory if successful
        try {
            memberRepository.removeMember(clan.getName(), target);
            // Only update memory after DB save succeeds
            clan.removeMember(target);
            playerClan.remove(target);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Player p = Bukkit.getPlayer(target);
            if (p != null) {
                p.sendMessage("§cLỗi khi xử lý kick member! Vui lòng thử lại sau.");
            }
            return false;
        }
    }

    public void disband(Clan clan) {
        if (clan == null) return;
        String clanName = clan.getName();

        // Save member data before deletion for rollback if needed
        java.util.Map<UUID, String> membersWithRoles = null;
        try {
            membersWithRoles = memberRepository.loadMembersWithRoles(clanName);
        } catch (SQLException e) {
            e.printStackTrace();
            Player owner = Bukkit.getPlayer(clan.getOwner());
            if (owner != null) {
                owner.sendMessage("§cLỗi khi giải tán clan! Vui lòng thử lại sau.");
            }
            return;
        }

        // Delete from DB first, only update memory if successful
        try {
            // Delete members first
            memberRepository.deleteAllMembers(clanName);
            
            // Then delete clan - if this fails, we need to restore members
            try {
                clanRepository.deleteClan(clanName);
            } catch (SQLException e) {
                // Rollback: restore members if clan deletion fails
                if (membersWithRoles != null && !membersWithRoles.isEmpty()) {
                    java.util.Set<UUID> successfullyRestored = new java.util.HashSet<>();
                    java.util.Set<UUID> failedToRestore = new java.util.HashSet<>();
                    
                    for (java.util.Map.Entry<UUID, String> entry : membersWithRoles.entrySet()) {
                        try {
                            memberRepository.addMember(clanName, entry.getKey(), entry.getValue());
                            successfullyRestored.add(entry.getKey());
                        } catch (SQLException rollbackException) {
                            rollbackException.printStackTrace();
                            failedToRestore.add(entry.getKey());
                            // Log but continue restoring other members
                        }
                    }
                    
                    // Verify restoration completeness
                    if (!failedToRestore.isEmpty()) {
                        System.err.println("[ClanCore] CRITICAL: Failed to restore " + failedToRestore.size() + 
                                " members during clan disband rollback. Clan: " + clanName + 
                                ". Database may be in inconsistent state. Failed UUIDs: " + failedToRestore);
                    }
                    
                    if (successfullyRestored.size() != membersWithRoles.size()) {
                        System.err.println("[ClanCore] WARNING: Only restored " + successfullyRestored.size() + 
                                " out of " + membersWithRoles.size() + " members for clan " + clanName + 
                                ". Some members may be missing from the database.");
                    }
                }
                throw e; // Re-throw to trigger outer catch
            }
            
            // Only update memory after DB delete succeeds
            // Remove buffs from all online members before disbanding
            if (buffManager != null) {
                for (UUID uuid : new HashSet<>(clan.getMembers())) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && member.isOnline()) {
                        buffManager.removeBuffs(member);
                    }
                }
            }
            clans.values().removeIf(c -> c == clan);
            for (UUID uuid : new HashSet<>(playerClan.keySet())) {
                if (playerClan.get(uuid) == clan) {
                    playerClan.remove(uuid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Player owner = Bukkit.getPlayer(clan.getOwner());
            if (owner != null) {
                owner.sendMessage("§cLỗi khi giải tán clan! Vui lòng thử lại sau.");
            }
        }
    }

    /**
     * Gửi yêu cầu chuyển quyền sở hữu clan cho thành viên khác.
     */
    public boolean requestTransferOwnership(Clan clan, UUID newOwnerUuid) {
        if (clan == null) return false;
        if (!clan.hasMember(newOwnerUuid)) return false; // newOwner phải là member
        if (clan.getOwner().equals(newOwnerUuid)) return false; // đã là owner rồi
        
        clan.setPendingTransferTo(newOwnerUuid);
        return true;
    }

    /**
     * Chấp nhận chuyển quyền sở hữu clan (sau khi đã có request).
     */
    public boolean acceptTransferOwnership(Clan clan) {
        if (clan == null) return false;
        UUID newOwnerUuid = clan.getPendingTransferTo();
        if (newOwnerUuid == null) return false; // Không có request nào
        
        UUID oldOwner = clan.getOwner();
        if (oldOwner.equals(newOwnerUuid)) return false; // Đã là owner rồi
        
        // Update DB - perform member role changes FIRST, then owner update
        // This ensures owner in clans table only updates after all role changes succeed
        try {
            // Step 1: Remove old owner from clan_members (will be re-added as MEMBER)
            try {
                memberRepository.removeMember(clan.getName(), oldOwner);
            } catch (SQLException e) {
                // No rollback needed - nothing changed yet
                throw e;
            }
            
            // Step 2: Add old owner back as MEMBER
            try {
                memberRepository.addMember(clan.getName(), oldOwner, "MEMBER");
            } catch (SQLException e) {
                // Rollback Step 1: restore old owner as OWNER
                try {
                    memberRepository.addMember(clan.getName(), oldOwner, "OWNER");
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
                throw e;
            }
            
            // Step 3: Remove new owner from clan_members (will be re-added as OWNER)
            try {
                memberRepository.removeMember(clan.getName(), newOwnerUuid);
            } catch (SQLException e) {
                // Rollback Steps 1-2: restore old owner as OWNER
                // Note: newOwnerUuid is still MEMBER in DB (removal failed), so don't try to re-add them
                try {
                    memberRepository.removeMember(clan.getName(), oldOwner);
                    memberRepository.addMember(clan.getName(), oldOwner, "OWNER");
                    // newOwnerUuid is already MEMBER in DB, no need to re-add
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
                throw e;
            }
            
            // Step 4: Add new owner as OWNER
            try {
                memberRepository.addMember(clan.getName(), newOwnerUuid, "OWNER");
            } catch (SQLException e) {
                // Rollback Steps 1-3: restore old owner as OWNER and new owner as MEMBER
                // Note: newOwnerUuid was already removed in Step 3, so we don't need to remove them again
                // We just need to add them back as MEMBER, then restore old owner as OWNER
                try {
                    // Add new owner back as MEMBER (they were removed in Step 3)
                    memberRepository.addMember(clan.getName(), newOwnerUuid, "MEMBER");
                    // Remove old owner (they're currently MEMBER from Step 2)
                    memberRepository.removeMember(clan.getName(), oldOwner);
                    // Add old owner back as OWNER
                    memberRepository.addMember(clan.getName(), oldOwner, "OWNER");
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
                throw e;
            }
            
            // Step 5: Update clan owner in clans table (LAST - only after all role changes succeed)
            try {
                clan.setOwner(newOwnerUuid); // Update in-memory first
                clanRepository.saveClan(clan);
            } catch (SQLException e) {
                // Rollback Steps 1-4: restore old owner as OWNER and new owner as MEMBER
                // Always restore in-memory state first, even if DB rollback fails
                // This ensures in-memory consistency even if database rollback is partial
                boolean rollbackSuccess = true;
                try {
                    memberRepository.removeMember(clan.getName(), newOwnerUuid);
                    memberRepository.addMember(clan.getName(), newOwnerUuid, "MEMBER");
                    memberRepository.removeMember(clan.getName(), oldOwner);
                    memberRepository.addMember(clan.getName(), oldOwner, "OWNER");
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                    rollbackSuccess = false;
                    System.err.println("[ClanCore] CRITICAL: Failed to rollback member roles in database during ownership transfer. " +
                            "Clan: " + clan.getName() + ". Database may be in inconsistent state.");
                }
                
                // Always restore in-memory owner, regardless of DB rollback success
                // This prevents in-memory/database inconsistency
                clan.setOwner(oldOwner);
                
                if (!rollbackSuccess) {
                    System.err.println("[ClanCore] WARNING: In-memory owner restored to " + oldOwner + 
                            ", but database rollback may have failed. Database and memory may be inconsistent.");
                }
                
                throw e;
            }
            
            // Clear pending transfer ONLY after all DB operations succeed
            clan.setPendingTransferTo(null);
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            // Rollback in-memory state (if it was updated)
            clan.setOwner(oldOwner);
            // Pending transfer is still set (we never cleared it), so no need to restore
            return false;
        }
    }

    /**
     * Từ chối hoặc hủy yêu cầu chuyển quyền sở hữu.
     */
    public void cancelTransferOwnership(Clan clan) {
        if (clan == null) return;
        clan.setPendingTransferTo(null);
    }

    /**
     * Update level và contribution của clan vào DB.
     * Gọi method này mỗi khi level hoặc contribution thay đổi.
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateClanStats(Clan clan) {
        if (clan == null) return false;
        try {
            clanRepository.updateClanStats(clan);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private BuffManager buffManager;

    public void setBuffManager(BuffManager buffManager) {
        this.buffManager = buffManager;
    }

    /**
     * Set level của clan và tự động save vào DB.
     * @return true nếu thành công, false nếu thất bại
     * 
     * Note: This method saves clan_points to DB via updateClanStats, but does NOT
     * rollback clan_points on failure. The caller (e.g., upgradeClan) is responsible
     * for managing clan_points rollback since it owns the points deduction logic.
     */
    public boolean setClanLevel(Clan clan, int level) {
        if (clan == null) return false;
        
        // Validate that level configuration exists before upgrading
        if (configManager == null) {
            return false; // Cannot upgrade without config manager
        }
        
        ConfigManager.LevelConfig levelConfig = configManager.getLevelConfig(level);
        if (levelConfig == null) {
            // Level configuration missing - cannot upgrade to this level
            return false;
        }
        
        // Save old values for rollback (only level and maxSlots, NOT clan_points)
        int oldLevel = clan.getLevel();
        int oldMaxSlots = clan.getMaxSlots();
        
        // Update in-memory first
        clan.setLevel(level);
        
        // Update maxSlots based on new level (we know levelConfig exists from validation above)
        clan.setMaxSlots(levelConfig.getMaxMembers());
        
        // Save to DB - rollback if save fails
        // Note: updateClanStats saves level, contribution, AND clan_points
        // We only rollback level and maxSlots here. The caller (upgradeClan) is
        // responsible for rolling back clan_points since it owns that logic.
        if (!updateClanStats(clan)) {
            // Rollback in-memory changes (only level and maxSlots, NOT clan_points)
            clan.setLevel(oldLevel);
            clan.setMaxSlots(oldMaxSlots);
            return false;
        }
        
        // Apply buffs cho tất cả members khi level up
        if (buffManager != null) {
            buffManager.updateClanBuffs(clan);
        }
        return true;
    }

    /**
     * Add contribution cho clan và tự động save vào DB.
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean addClanContribution(Clan clan, int amount) {
        if (clan == null || amount <= 0) return false;
        
        // Save to DB first, only update memory if successful
        int oldContribution = clan.getContribution();
        clan.addContribution(amount);
        
        if (updateClanStats(clan)) {
            // DB save succeeded - memory already updated
            return true;
        } else {
            // DB save failed - rollback in-memory state
            clan.setContribution(oldContribution);
            return false;
        }
    }
}
