package me.skibidi.clancore.team;

import me.skibidi.clancore.team.model.Team;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private static final int DEFAULT_MAX_SIZE = 5;

    private final Map<UUID, Team> playerTeam = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> pendingInvites = new HashMap<>(); // target -> (leader -> timestamp)

    public Team getTeam(Player player) {
        return playerTeam.get(player.getUniqueId());
    }

    public boolean isInTeam(Player player) {
        return getTeam(player) != null;
    }

    public boolean sameTeam(Player a, Player b) {
        Team ta = getTeam(a);
        Team tb = getTeam(b);
        return ta != null && ta == tb;
    }

    /**
     * Lấy tất cả teams hiện có trong server.
     */
    public java.util.Collection<Team> getAllTeams() {
        return java.util.Collections.unmodifiableCollection(
                new java.util.HashSet<>(playerTeam.values())
        );
    }

    public boolean createTeam(Player leader) {
        if (isInTeam(leader)) return false;

        Team team = new Team(leader.getUniqueId(), DEFAULT_MAX_SIZE);
        playerTeam.put(leader.getUniqueId(), team);
        return true;
    }

    public boolean invite(Player leader, Player target) {
        Team team = getTeam(leader);
        if (team == null) return false;
        if (!team.getLeader().equals(leader.getUniqueId())) return false;
        if (isInTeam(target)) return false;

        // Store timestamp for 30-second expiration
        pendingInvites.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>())
                .put(leader.getUniqueId(), System.currentTimeMillis());
        return true;
    }
    
    /**
     * Kiểm tra xem invite có còn hiệu lực không (30 giây).
     */
    private boolean isInviteValid(UUID target, UUID leader) {
        Map<UUID, Long> leaderInvites = pendingInvites.get(target);
        if (leaderInvites == null) return false;
        Long timestamp = leaderInvites.get(leader);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < 30000; // 30 seconds
    }
    
    /**
     * Xóa các invites đã hết hạn cho một player.
     */
    private void cleanupExpiredInvites(UUID target) {
        Map<UUID, Long> leaderInvites = pendingInvites.get(target);
        if (leaderInvites == null) return;
        long now = System.currentTimeMillis();
        leaderInvites.entrySet().removeIf(entry -> (now - entry.getValue()) >= 30000);
        if (leaderInvites.isEmpty()) {
            pendingInvites.remove(target);
        }
    }

    public boolean accept(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInTeam(player)) return false;

        Map<UUID, Long> leaderInvites = pendingInvites.get(uuid);
        if (leaderInvites == null || leaderInvites.isEmpty()) return false;
        
        // Clean up expired invites first
        cleanupExpiredInvites(uuid);
        leaderInvites = pendingInvites.get(uuid);
        if (leaderInvites == null || leaderInvites.isEmpty()) {
            player.sendMessage("§cTất cả lời mời đã hết hạn (30 giây).");
            return false;
        }

        // Lấy invite đầu tiên có thể join
        boolean foundAnyTeam = false; // Track if any team exists (full or not)
        boolean foundFullTeam = false; // Track if any team is full
        boolean foundExpiredInvite = false; // Track if any invite expired
        
        for (UUID leaderId : new HashSet<>(leaderInvites.keySet())) { // Create copy to avoid concurrent modification
            // Check expiration
            if (!isInviteValid(uuid, leaderId)) {
                foundExpiredInvite = true;
                leaderInvites.remove(leaderId);
                continue;
            }
            
            Team team = null;
            for (Team t : new HashSet<>(playerTeam.values())) {
                if (t.getLeader().equals(leaderId)) {
                    team = t;
                    break;
                }
            }
            
            if (team != null) {
                foundAnyTeam = true;
                
                // Check capacity BEFORE attempting to join (prevent late acceptances causing over-capacity)
                if (team.getMembers().size() >= team.getMaxSize()) {
                    foundFullTeam = true; // Team exists but is full
                    // Remove expired invite
                    leaderInvites.remove(leaderId);
                    continue;
                }
                
                // Check again atomically before joining to prevent TOCTOU race condition
                if (playerTeam.containsKey(uuid)) {
                    // Player already in a team, skip this invite and continue to next
                    continue;
                }
                
                // Final capacity check RIGHT BEFORE adding (safety check)
                if (team.getMembers().size() >= team.getMaxSize()) {
                    // Team became full between checks - remove invite and try next
                    leaderInvites.remove(leaderId);
                    foundFullTeam = true;
                    continue;
                }
                
                // Successfully joining - now remove all invites
                team.addMember(uuid);
                playerTeam.put(uuid, team);
                pendingInvites.remove(uuid); // Remove all invites after successful join
                return true;
            } else {
                // Team doesn't exist (disbanded) - remove invite
                leaderInvites.remove(leaderId);
            }
        }
        
        // Clean up empty invite map
        if (leaderInvites.isEmpty()) {
            pendingInvites.remove(uuid);
        }
        
        if (foundFullTeam) {
            // All invited teams are full
            player.sendMessage("§cTất cả team bạn được mời đều đã đầy. Các lời mời đã bị hủy.");
        } else if (foundExpiredInvite) {
            // Some invites expired
            player.sendMessage("§cTất cả lời mời đã hết hạn (30 giây).");
        } else if (foundAnyTeam) {
            // Teams exist but something else went wrong
            player.sendMessage("§cKhông thể tham gia team. Các lời mời đã bị hủy.");
        } else {
            // Teams have disbanded or leaders no longer exist
            player.sendMessage("§cKhông tìm thấy team nào. Các lời mời đã bị hủy.");
        }
        
        return false;
    }

    public boolean leave(Player player) {
        UUID uuid = player.getUniqueId();
        Team team = getTeam(player);
        if (team == null) return false;

        if (team.getLeader().equals(uuid)) {
            // leader rời -> disband
            disband(player);
            return true;
        }

        team.removeMember(uuid);
        playerTeam.remove(uuid);
        return true;
    }

    public boolean kick(Player leader, Player target) {
        Team team = getTeam(leader);
        if (team == null) return false;
        if (!team.getLeader().equals(leader.getUniqueId())) return false;

        if (!team.isMember(target.getUniqueId())) return false;

        team.removeMember(target.getUniqueId());
        playerTeam.remove(target.getUniqueId());
        return true;
    }

    public boolean disband(Player leader) {
        Team team = getTeam(leader);
        if (team == null) return false;
        if (!team.getLeader().equals(leader.getUniqueId())) return false;

        for (UUID member : new HashSet<>(team.getMembers())) {
            playerTeam.remove(member);
        }
        return true;
    }

    public void removeFromTeam(Player player) {
        Team team = getTeam(player);
        if (team == null) return;

        UUID uuid = player.getUniqueId();
        boolean wasLeader = team.getLeader().equals(uuid);
        
        team.removeMember(uuid);
        playerTeam.remove(uuid);

        // Nếu là leader
        if (wasLeader) {
            // Nếu không còn thành viên nào -> disband team
            if (team.getMembers().isEmpty()) {
                // Team đã được cleanup khi remove member cuối cùng
                // Không cần làm gì thêm vì team object sẽ bị GC
                return;
            }
            
            // Còn thành viên -> promote member đầu tiên làm leader mới
            // Create a copy to avoid ConcurrentModificationException
            UUID newLeader = null;
            for (UUID member : new java.util.HashSet<>(team.getMembers())) {
                if (!member.equals(uuid)) {
                    newLeader = member;
                    break;
                }
            }
            
            if (newLeader != null) {
                // Promote member đầu tiên làm leader
                promoteLeader(team, newLeader);
            }
        }
    }

    /**
     * Promote một member lên làm leader mới của team.
     */
    private void promoteLeader(Team team, UUID newLeader) {
        if (!team.isMember(newLeader)) {
            return; // newLeader phải là member của team
        }
        
        // Set leader mới
        team.setLeader(newLeader);
        
        // Thông báo cho các members online
        org.bukkit.OfflinePlayer offlineNewLeader = org.bukkit.Bukkit.getOfflinePlayer(newLeader);
        String newLeaderName = offlineNewLeader.getName() != null ? offlineNewLeader.getName() : "Unknown";
        
        // Create a copy to avoid ConcurrentModificationException
        for (UUID memberUuid : new java.util.HashSet<>(team.getMembers())) {
            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage("§6" + newLeaderName + " §eđã trở thành leader mới của team!");
            }
        }
    }

    /**
     * Gửi yêu cầu chuyển quyền sở hữu team cho thành viên khác.
     */
    public boolean requestTransferOwnership(Team team, UUID newLeaderUuid) {
        if (team == null) return false;
        if (!team.isMember(newLeaderUuid)) return false; // newLeader phải là member
        if (team.getLeader().equals(newLeaderUuid)) return false; // đã là leader rồi
        
        team.setPendingTransferTo(newLeaderUuid);
        return true;
    }

    /**
     * Chấp nhận chuyển quyền sở hữu team (sau khi đã có request).
     */
    public boolean acceptTransferOwnership(Team team) {
        if (team == null) return false;
        UUID newLeaderUuid = team.getPendingTransferTo();
        if (newLeaderUuid == null) return false; // Không có request nào
        
        UUID oldLeader = team.getLeader();
        if (oldLeader.equals(newLeaderUuid)) return false; // Đã là leader rồi
        
        // Update leader (DON'T clear pending transfer yet - only clear after success)
        team.setLeader(newLeaderUuid);
        
        // Clear pending transfer after successful update
        team.setPendingTransferTo(null);
        
        // Thông báo cho các members online
        for (UUID memberUuid : team.getMembers()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                Player newLeaderPlayer = org.bukkit.Bukkit.getPlayer(newLeaderUuid);
                if (newLeaderPlayer != null) {
                    member.sendMessage("§6" + newLeaderPlayer.getName() + " §eđã trở thành leader mới của team!");
                }
            }
        }
        
        return true;
    }

    /**
     * Từ chối hoặc hủy yêu cầu chuyển quyền sở hữu.
     */
    public void cancelTransferOwnership(Team team) {
        if (team == null) return;
        team.setPendingTransferTo(null);
    }
}
