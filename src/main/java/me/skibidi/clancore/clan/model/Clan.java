package me.skibidi.clancore.clan.model;

import java.util.HashSet;
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
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final Set<UUID> joinRequests = new HashSet<>();

    private int maxSlots = 50;
    private int level = 1;
    private int contribution = 0;
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

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvitedPlayers() {
        return invitedPlayers;
    }

    public Set<UUID> getJoinRequests() {
        return joinRequests;
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

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean hasMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isFull() {
        return members.size() >= maxSlots;
    }
}
