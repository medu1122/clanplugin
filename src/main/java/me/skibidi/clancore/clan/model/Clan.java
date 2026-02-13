package me.skibidi.clancore.clan.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory Clan model theo thiết kế trong CONTEXT.md.
 * Hiện tại lưu trong RAM, có thể map sang SQLite sau này.
 */
public class Clan {

    private final String name;
    private UUID owner;

    private final Set<UUID> members = new HashSet<>();
    private final Map<UUID, Long> invitedPlayers = new HashMap<>(); // UUID -> timestamp (millis)
    private final Map<UUID, Long> joinRequests = new HashMap<>(); // UUID -> timestamp (millis)
    private UUID pendingTransferTo; // Người được đề xuất nhận quyền sở hữu

    private int maxSlots = 50;
    private int level = 1;
    private int contribution = 0;
    private int clanPoints = 0; // Điểm riêng để upgrade clan
    private String bannerData;

    public Clan(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Map<UUID, Long> getInvitedPlayers() {
        return invitedPlayers;
    }

    public Map<UUID, Long> getJoinRequests() {
        return joinRequests;
    }
    
    /**
     * Kiểm tra xem invite có còn hiệu lực không (30 giây).
     */
    public boolean isInviteValid(UUID uuid) {
        Long timestamp = invitedPlayers.get(uuid);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < 30000; // 30 seconds
    }
    
    /**
     * Kiểm tra xem join request có còn hiệu lực không (30 giây).
     */
    public boolean isJoinRequestValid(UUID uuid) {
        Long timestamp = joinRequests.get(uuid);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < 30000; // 30 seconds
    }
    
    /**
     * Xóa các invites đã hết hạn.
     */
    public void cleanupExpiredInvites() {
        long now = System.currentTimeMillis();
        invitedPlayers.entrySet().removeIf(entry -> (now - entry.getValue()) >= 30000);
    }
    
    /**
     * Xóa các join requests đã hết hạn.
     */
    public void cleanupExpiredRequests() {
        long now = System.currentTimeMillis();
        joinRequests.entrySet().removeIf(entry -> (now - entry.getValue()) >= 30000);
    }

    public UUID getPendingTransferTo() {
        return pendingTransferTo;
    }

    public void setPendingTransferTo(UUID uuid) {
        this.pendingTransferTo = uuid;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public void setMaxSlots(int maxSlots) {
        this.maxSlots = maxSlots;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = Math.max(0, contribution);
    }

    public void addContribution(int amount) {
        if (amount > 0) {
            this.contribution += amount;
        }
    }

    public String getBannerData() {
        return bannerData;
    }

    public void setBannerData(String bannerData) {
        this.bannerData = bannerData;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
        joinRequests.remove(uuid);
        invitedPlayers.remove(uuid);
    }
    
    /**
     * Kiểm tra xem clan có còn chỗ không trước khi add member.
     */
    public boolean canAddMember() {
        return members.size() < maxSlots;
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean hasMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isFull() {
        return members.size() >= maxSlots;
    }

    public int getClanPoints() {
        return clanPoints;
    }

    public void setClanPoints(int clanPoints) {
        this.clanPoints = Math.max(0, clanPoints);
    }

    public void addClanPoints(int amount) {
        if (amount > 0) {
            this.clanPoints += amount;
        }
    }

    public boolean removeClanPoints(int amount) {
        if (amount > 0 && this.clanPoints >= amount) {
            this.clanPoints -= amount;
            return true;
        }
        return false;
    }
}
