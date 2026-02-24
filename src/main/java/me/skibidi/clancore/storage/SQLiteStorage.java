package me.skibidi.clancore.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteStorage {

    private final DatabaseManager databaseManager;

    public SQLiteStorage(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initTables() throws SQLException {
        Connection connection = databaseManager.getConnection();
        try (Statement stmt = connection.createStatement()) {

            // CLANS TABLE
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    owner TEXT NOT NULL,
                    level INTEGER DEFAULT 1,
                    contribution INTEGER DEFAULT 0,
                    clan_points INTEGER DEFAULT 0
                );
            """);

            // CLAN MEMBERS TABLE
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_members (
                    clan_id INTEGER NOT NULL,
                    uuid TEXT NOT NULL,
                    role TEXT DEFAULT 'MEMBER',
                    PRIMARY KEY (clan_id, uuid),
                    FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE
                );
            """);
            
            // Clan war toggles (chủ clan bật/tắt war với từng clan)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_war_toggles (
                    clan_name TEXT NOT NULL,
                    target_clan_name TEXT NOT NULL,
                    PRIMARY KEY (clan_name, target_clan_name)
                );
            """);

            // Clan flags (Base - cờ đã cắm)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_flags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    clan_name TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    banner_color TEXT DEFAULT 'RED'
                );
            """);

            // Kho cờ: inventory 54 slot (chỉ đá quý), mỗi slot lưu số lượng
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS flag_inventory (
                    flag_id INTEGER NOT NULL,
                    slot INTEGER NOT NULL,
                    amount INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (flag_id, slot),
                    FOREIGN KEY (flag_id) REFERENCES clan_flags(id) ON DELETE CASCADE
                );
            """);

            // Quyền mở kho cờ (rút/đặt đá quý)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS flag_permissions (
                    flag_id INTEGER NOT NULL,
                    uuid TEXT NOT NULL,
                    can_open INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (flag_id, uuid),
                    FOREIGN KEY (flag_id) REFERENCES clan_flags(id) ON DELETE CASCADE
                );
            """);

            // Số cờ đã lấy từ pool (level 5+ mỗi level 1 cờ)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clan_flag_pool (
                    clan_name TEXT PRIMARY KEY,
                    taken_count INTEGER NOT NULL DEFAULT 0
                );
            """);

            migrateClanPointsColumn(connection);
        }
    }
    
    /**
     * Migrate existing databases by adding clan_points column if it doesn't exist.
     * This handles upgrades from previous versions that didn't have this column.
     */
    private void migrateClanPointsColumn(Connection connection) throws SQLException {
        try {
            // Check if clan_points column exists
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet columns = metaData.getColumns(null, null, "clans", "clan_points")) {
                if (!columns.next()) {
                    // Column doesn't exist, add it
                    try (Statement stmt = connection.createStatement()) {
                        stmt.executeUpdate("ALTER TABLE clans ADD COLUMN clan_points INTEGER DEFAULT 0");
                        System.out.println("[ClanCore] Migrated database: Added clan_points column to clans table.");
                    }
                }
            }
        } catch (SQLException e) {
            // If migration fails, log error but don't crash - plugin might still work
            System.err.println("[ClanCore] WARNING: Failed to migrate clan_points column: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
