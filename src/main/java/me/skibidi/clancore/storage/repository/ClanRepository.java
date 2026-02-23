package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.clan.model.Clan;
import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.UUID;

public class ClanRepository {

    private final DatabaseManager databaseManager;

    public ClanRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Lưu clan mới vào DB hoặc update nếu đã tồn tại.
     */
    public void saveClan(Clan clan) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clans (name, owner, level, contribution, clan_points) VALUES (?, ?, ?, ?, 0) " +
                        "ON CONFLICT(name) DO UPDATE SET owner = ?, level = ?, contribution = ?, clan_points = 0"
        )) {
            stmt.setString(1, clan.getName());
            stmt.setString(2, clan.getOwner().toString());
            stmt.setInt(3, clan.getLevel());
            stmt.setInt(4, clan.getContribution());
            stmt.setString(5, clan.getOwner().toString());
            stmt.setInt(6, clan.getLevel());
            stmt.setInt(7, clan.getContribution());
            stmt.executeUpdate();
        }
    }

    /**
     * Load clan từ DB theo tên.
     */
    public Clan loadClan(String name) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, name, owner, level, contribution, clan_points FROM clans WHERE name = ?"
        )) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    Clan clan = new Clan(rs.getString("name"), owner);
                    clan.setLevel(rs.getInt("level"));
                    clan.setContribution(rs.getInt("contribution"));
                    return clan;
                }
            }
        }
        return null;
    }

    /**
     * Load tất cả clans từ DB.
     */
    public java.util.List<Clan> loadAllClans() throws SQLException {
        java.util.List<Clan> clans = new java.util.ArrayList<>();
        Connection conn = databaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, owner, level, contribution, clan_points FROM clans")) {
            while (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                Clan clan = new Clan(rs.getString("name"), owner);
                clan.setLevel(rs.getInt("level"));
                clan.setContribution(rs.getInt("contribution"));
                clans.add(clan);
            }
        }
        return clans;
    }

    /**
     * Update level và contribution của clan.
     */
    public void updateClanStats(Clan clan) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE clans SET level = ?, contribution = ?, clan_points = 0 WHERE name = ?"
        )) {
            stmt.setInt(1, clan.getLevel());
            stmt.setInt(2, clan.getContribution());
            stmt.setString(3, clan.getName());
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa clan khỏi DB.
     */
    public void deleteClan(String name) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clans WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }
}
