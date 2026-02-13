package me.skibidi.clancore.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "clans.db");

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        plugin.getLogger().info("SQLite connected successfully.");
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database not connected.");
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("SQLite connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
