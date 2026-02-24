package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;

public class ClanFlagPoolRepository {

    private final DatabaseManager databaseManager;

    public ClanFlagPoolRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public int getTakenCount(String clanName) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT taken_count FROM clan_flag_pool WHERE clan_name = ?")) {
            stmt.setString(1, clanName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("taken_count") : 0;
            }
        }
    }

    public void incrementTaken(String clanName) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clan_flag_pool (clan_name, taken_count) VALUES (?, 1) " +
                "ON CONFLICT(clan_name) DO UPDATE SET taken_count = taken_count + 1")) {
            stmt.setString(1, clanName);
            stmt.executeUpdate();
        }
    }
}
