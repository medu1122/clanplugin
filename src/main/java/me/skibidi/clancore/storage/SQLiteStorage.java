package me.skibidi.clancore.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteStorage {

    private final DatabaseManager databaseManager;

    public SQLiteStorage(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initTables() throws SQLException {

        try (Connection connection = databaseManager.getConnection();
             Statement stmt = connection.createStatement()) {

            // CLANS TABLE
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    owner TEXT NOT NULL,
                    level INTEGER DEFAULT 1,
                    contribution INTEGER DEFAULT 0
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
        }
    }
}
