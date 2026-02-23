package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.*;

public class ClanWarRepository {

    private final DatabaseManager databaseManager;

    public ClanWarRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Load tất cả war toggles: map clanName -> set of target clan names (war enabled).
     */
    public Map<String, Set<String>> loadAllWarToggles() throws SQLException {
        Map<String, Set<String>> map = new HashMap<>();
        Connection conn = databaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clan_name, target_clan_name FROM clan_war_toggles")) {
            while (rs.next()) {
                String clan = rs.getString("clan_name");
                String target = rs.getString("target_clan_name");
                map.computeIfAbsent(clan, k -> new HashSet<>()).add(target);
            }
        }
        return map;
    }

    public void setWarEnabled(String clanName, String targetClanName, boolean enabled) throws SQLException {
        Connection conn = databaseManager.getConnection();
        if (enabled) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR IGNORE INTO clan_war_toggles (clan_name, target_clan_name) VALUES (?, ?)")) {
                stmt.setString(1, clanName);
                stmt.setString(2, targetClanName);
                stmt.executeUpdate();
            }
        } else {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM clan_war_toggles WHERE clan_name = ? AND target_clan_name = ?")) {
                stmt.setString(1, clanName);
                stmt.setString(2, targetClanName);
                stmt.executeUpdate();
            }
        }
    }
}
