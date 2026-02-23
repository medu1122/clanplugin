package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.flag.model.ClanFlag;
import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClanFlagRepository {

    private final DatabaseManager databaseManager;

    public ClanFlagRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<ClanFlag> loadAll() throws SQLException {
        List<ClanFlag> list = new ArrayList<>();
        Connection conn = databaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, clan_name, world, x, y, z, banner_color FROM clan_flags")) {
            while (rs.next()) {
                list.add(new ClanFlag(
                    rs.getInt("id"),
                    rs.getString("clan_name"),
                    rs.getString("world"),
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    rs.getString("banner_color")
                ));
            }
        }
        return list;
    }

    /** Trả về id sau khi insert. */
    public int insert(String clanName, String world, int x, int y, int z, String bannerColor) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clan_flags (clan_name, world, x, y, z, banner_color) VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, clanName);
            stmt.setString(2, world);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setInt(5, z);
            stmt.setString(6, bannerColor != null ? bannerColor : "RED");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void delete(int id) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clan_flags WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteByClan(String clanName) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM clan_flags WHERE clan_name = ?")) {
            stmt.setString(1, clanName);
            stmt.executeUpdate();
        }
    }
}
