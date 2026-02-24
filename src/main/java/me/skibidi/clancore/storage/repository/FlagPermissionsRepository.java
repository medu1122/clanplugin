package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlagPermissionsRepository {

    private final DatabaseManager databaseManager;

    public FlagPermissionsRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Set<UUID> loadCanOpen(int flagId) throws SQLException {
        Set<UUID> set = new HashSet<>();
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT uuid FROM flag_permissions WHERE flag_id = ? AND can_open = 1")) {
            stmt.setInt(1, flagId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        set.add(UUID.fromString(rs.getString("uuid")));
                    } catch (Exception ignored) {}
                }
            }
        }
        return set;
    }

    public void setCanOpen(int flagId, UUID uuid, boolean canOpen) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO flag_permissions (flag_id, uuid, can_open) VALUES (?, ?, ?)")) {
            stmt.setInt(1, flagId);
            stmt.setString(2, uuid.toString());
            stmt.setInt(3, canOpen ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    public void deleteByFlagId(int flagId) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM flag_permissions WHERE flag_id = ?")) {
            stmt.setInt(1, flagId);
            stmt.executeUpdate();
        }
    }
}
