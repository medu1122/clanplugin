package me.skibidi.clancore.storage.repository;

import me.skibidi.clancore.storage.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FlagInventoryRepository {

    private static final int SLOTS = 54;
    private final DatabaseManager databaseManager;

    public FlagInventoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /** Load 54 slots: slot -> amount. */
    public Map<Integer, Integer> load(int flagId) throws SQLException {
        Map<Integer, Integer> inv = new HashMap<>();
        for (int i = 0; i < SLOTS; i++) inv.put(i, 0);
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT slot, amount FROM flag_inventory WHERE flag_id = ?")) {
            stmt.setInt(1, flagId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    inv.put(rs.getInt("slot"), rs.getInt("amount"));
                }
            }
        }
        return inv;
    }

    public void save(int flagId, Map<Integer, Integer> slots) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM flag_inventory WHERE flag_id = ?");
             PreparedStatement ins = conn.prepareStatement("INSERT OR REPLACE INTO flag_inventory (flag_id, slot, amount) VALUES (?, ?, ?)")) {
            del.setInt(1, flagId);
            del.executeUpdate();
            for (Map.Entry<Integer, Integer> e : slots.entrySet()) {
                if (e.getValue() == null || e.getValue() <= 0) continue;
                ins.setInt(1, flagId);
                ins.setInt(2, e.getKey());
                ins.setInt(3, e.getValue());
                ins.executeUpdate();
            }
        }
    }

    public void deleteByFlagId(int flagId) throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM flag_inventory WHERE flag_id = ?")) {
            stmt.setInt(1, flagId);
            stmt.executeUpdate();
        }
    }
}
