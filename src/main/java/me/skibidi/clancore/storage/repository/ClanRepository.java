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
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO clans (name, owner, level, contribution, clan_points) VALUES (?, ?, ?, ?, ?) " +
                             "ON CONFLICT(name) DO UPDATE SET owner = ?, level = ?, contribution = ?, clan_points = ?"
             )) {
            stmt.setString(1, clan.getName());
            stmt.setString(2, clan.getOwner().toString());
            stmt.setInt(3, clan.getLevel());
            stmt.setInt(4, clan.getContribution());
            stmt.setInt(5, clan.getClanPoints());
            stmt.setString(6, clan.getOwner().toString());
            stmt.setInt(7, clan.getLevel());
            stmt.setInt(8, clan.getContribution());
            stmt.setInt(9, clan.getClanPoints());
            stmt.executeUpdate();
        }
    }

    /**
     * Load clan từ DB theo tên.
     */
    public Clan loadClan(String name) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, name, owner, level, contribution, clan_points FROM clans WHERE name = ?"
             )) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                Clan clan = new Clan(rs.getString("name"), owner);
                clan.setLevel(rs.getInt("level"));
                clan.setContribution(rs.getInt("contribution"));
                clan.setClanPoints(rs.getInt("clan_points"));
                return clan;
            }
        }
        return null;
    }

    /**
     * Load tất cả clans từ DB.
     */
    public java.util.List<Clan> loadAllClans() throws SQLException {
        java.util.List<Clan> clans = new java.util.ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, owner, level, contribution, clan_points FROM clans")) {
            while (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                Clan clan = new Clan(rs.getString("name"), owner);
                clan.setLevel(rs.getInt("level"));
                clan.setContribution(rs.getInt("contribution"));
                clan.setClanPoints(rs.getInt("clan_points"));
                clans.add(clan);
            }
        }
        return clans;
    }

    /**
     * Update level và contribution của clan.
     */
    public void updateClanStats(Clan clan) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE clans SET level = ?, contribution = ?, clan_points = ? WHERE name = ?"
             )) {
            stmt.setInt(1, clan.getLevel());
            stmt.setInt(2, clan.getContribution());
            stmt.setInt(3, clan.getClanPoints());
            stmt.setString(4, clan.getName());
            stmt.executeUpdate();
        }
    }

    /**
     * Xóa clan khỏi DB.
     */
    public void deleteClan(String name) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clans WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }
}
