package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanMemberRepository {

    private final DatabaseManager databaseManager;

    public ClanMemberRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Lưu member vào clan.
     */
    public void addMember(String clanName, UUID memberUuid, String role) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO clan_members (clan_id, uuid, role) " +
                             "SELECT id, ?, ? FROM clans WHERE name = ?"
             )) {
            stmt.setString(1, memberUuid.toString());
            stmt.setString(2, role);
            stmt.setString(3, clanName);
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa member khỏi clan.
     */
    public void removeMember(String clanName, UUID memberUuid) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM clan_members WHERE uuid = ? AND clan_id = (SELECT id FROM clans WHERE name = ?)"
             )) {
            stmt.setString(1, memberUuid.toString());
            stmt.setString(2, clanName);
            stmt.executeUpdate();
        }
    }

    /**
     * Load tất cả members của clan.
     */
    public List<UUID> loadMembers(String clanName) throws SQLException {
        List<UUID> members = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid FROM clan_members WHERE clan_id = (SELECT id FROM clans WHERE name = ?)"
             )) {
            stmt.setString(1, clanName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(UUID.fromString(rs.getString("uuid")));
            }
        }
        return members;
    }

    /**
     * Load tất cả members của clan kèm role (để restore khi rollback).
     */
    public java.util.Map<UUID, String> loadMembersWithRoles(String clanName) throws SQLException {
        java.util.Map<UUID, String> membersWithRoles = new java.util.HashMap<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid, role FROM clan_members WHERE clan_id = (SELECT id FROM clans WHERE name = ?)"
             )) {
            stmt.setString(1, clanName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                membersWithRoles.put(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("role")
                );
            }
        }
        return membersWithRoles;
    }

    /**
     * Xóa tất cả members của clan (khi disband).
     */
    public void deleteAllMembers(String clanName) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM clan_members WHERE clan_id = (SELECT id FROM clans WHERE name = ?)"
             )) {
            stmt.setString(1, clanName);
            stmt.executeUpdate();
        }
    }
}
