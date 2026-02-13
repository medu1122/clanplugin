package me.skibidi.clancore.team.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final int maxSize;

    public Team(UUID leader, int maxSize) {
        this.leader = leader;
        this.maxSize = maxSize;
        this.members.add(leader);
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}
