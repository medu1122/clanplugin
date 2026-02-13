package me.skibidi.clancore.clan;

import me.skibidi.clancore.clan.model.Clan;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanManager {

    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, Clan> playerClan = new HashMap<>();

    public boolean createClan(String name, Player owner) {
        if (name == null || name.isBlank()) return false;

        String key = name.toLowerCase(Locale.ROOT);
        if (clans.containsKey(key)) return false;
        if (playerClan.containsKey(owner.getUniqueId())) return false;

        Clan clan = new Clan(name, owner.getUniqueId());
        clans.put(key, clan);
        playerClan.put(owner.getUniqueId(), clan);
        return true;
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
        if (clan.isFull()) return;

        clan.addMember(player.getUniqueId());
        playerClan.put(player.getUniqueId(), clan);
    }

    public void leaveClan(Player player) {
        Clan clan = playerClan.remove(player.getUniqueId());
        if (clan != null) {
            clan.removeMember(player.getUniqueId());
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

        return clan.getInvitedPlayers().add(target.getUniqueId());
    }

    /**
     * Player chấp nhận invite vào clan (nếu có).
     */
    public boolean acceptInvite(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerClan.containsKey(uuid)) return false;

        for (Clan clan : clans.values()) {
            if (clan.getInvitedPlayers().contains(uuid)) {
                if (clan.isFull()) return false;
                clan.addMember(uuid);
                playerClan.put(uuid, clan);
                return true;
            }
        }
        return false;
    }

    public boolean denyInvite(Player player) {
        UUID uuid = player.getUniqueId();
        boolean removed = false;
        for (Clan clan : clans.values()) {
            removed |= clan.getInvitedPlayers().remove(uuid);
        }
        return removed;
    }

    // ========== JOIN REQUESTS ==========

    public boolean requestJoin(Player player, Clan clan) {
        if (clan == null) return false;
        if (playerClan.containsKey(player.getUniqueId())) return false;

        return clan.getJoinRequests().add(player.getUniqueId());
    }

    public Set<UUID> getJoinRequests(Clan clan) {
        return clan.getJoinRequests();
    }

    public boolean acceptRequest(Clan clan, UUID requester) {
        if (clan == null) return false;
        if (!clan.getJoinRequests().contains(requester)) return false;
        if (clan.isFull()) return false;

        clan.getJoinRequests().remove(requester);
        clan.addMember(requester);
        playerClan.put(requester, clan);
        return true;
    }

    public boolean denyRequest(Clan clan, UUID requester) {
        if (clan == null) return false;
        return clan.getJoinRequests().remove(requester);
    }

    // ========== MANAGEMENT ==========

    public boolean isOwner(Player player, Clan clan) {
        if (clan == null) return false;
        return clan.getOwner().equals(player.getUniqueId());
    }

    public boolean kickMember(Clan clan, UUID target) {
        if (clan == null) return false;
        if (clan.getOwner().equals(target)) return false; // không kick owner

        if (clan.hasMember(target)) {
            clan.removeMember(target);
            playerClan.remove(target);
            return true;
        }
        return false;
    }

    public void disband(Clan clan) {
        if (clan == null) return;
        clans.values().removeIf(c -> c == clan);
        for (UUID uuid : new HashSet<>(playerClan.keySet())) {
            if (playerClan.get(uuid) == clan) {
                playerClan.remove(uuid);
            }
        }
    }
}
