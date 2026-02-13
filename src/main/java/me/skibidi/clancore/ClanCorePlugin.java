package me.skibidi.clancore;

import me.skibidi.clancore.storage.DatabaseManager;
import me.skibidi.clancore.storage.SQLiteStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanCorePlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private SQLiteStorage sqliteStorage;

    @Override
    public void onEnable() {
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            sqliteStorage = new SQLiteStorage(databaseManager);
            sqliteStorage.initTables();

        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
