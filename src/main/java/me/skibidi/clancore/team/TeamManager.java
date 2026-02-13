package me.skibidi.clancore.team;

import me.skibidi.clancore.team.model.Team;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private static final int DEFAULT_MAX_SIZE = 5;

    private final Map<UUID, Team> playerTeam = new HashMap<>();
    private final Map<UUID, Set<UUID>> pendingInvites = new HashMap<>(); // target -> leaders

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

        pendingInvites.computeIfAbsent(target.getUniqueId(), k -> new HashSet<>())
                .add(leader.getUniqueId());
        return true;
    }

    public boolean accept(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInTeam(player)) return false;

        Set<UUID> leaders = pendingInvites.remove(uuid);
        if (leaders == null || leaders.isEmpty()) return false;

        // Lấy invite đầu tiên
        for (UUID leaderId : leaders) {
            Team team = null;
            for (Team t : new HashSet<>(playerTeam.values())) {
                if (t.getLeader().equals(leaderId)) {
                    team = t;
                    break;
                }
            }
            if (team != null && !team.isFull()) {
                team.addMember(uuid);
                playerTeam.put(uuid, team);
                return true;
            }
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
        team.removeMember(uuid);
        playerTeam.remove(uuid);

        // nếu là leader & còn thành viên -> promote tạm thời member đầu tiên
        if (team.getLeader().equals(uuid) && !team.getMembers().isEmpty()) {
            // Team model hiện tại không cho đổi leader, nên nếu leader out thì coi như disband
            for (UUID member : new HashSet<>(team.getMembers())) {
                playerTeam.remove(member);
            }
        }
    }
}
