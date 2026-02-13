package me.skibidi.clancore;

import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.commands.ClanCommand;
import me.skibidi.clancore.commands.TeamCommand;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.listeners.JoinListener;
import me.skibidi.clancore.listeners.PvPListener;
import me.skibidi.clancore.listeners.QuitListener;
import me.skibidi.clancore.storage.DatabaseManager;
import me.skibidi.clancore.storage.SQLiteStorage;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanCorePlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private SQLiteStorage sqliteStorage;
    private ClanManager clanManager;
    private TeamManager teamManager;
    private WarManager warManager;
    private EspManager espManager;

    @Override
    public void onEnable() {
        try {
            // Database + storage
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            sqliteStorage = new SQLiteStorage(databaseManager);
            sqliteStorage.initTables();

            // In-memory managers
            clanManager = new ClanManager();
            teamManager = new TeamManager();
            warManager = new WarManager();
            espManager = new EspManager(clanManager, warManager);

            // Commands
            getCommand("clan").setExecutor(new ClanCommand(clanManager, warManager, espManager));
            getCommand("team").setExecutor(new TeamCommand(teamManager));

            // Listeners
            getServer().getPluginManager().registerEvents(new JoinListener(espManager), this);
            getServer().getPluginManager().registerEvents(new QuitListener(teamManager, espManager), this);
            getServer().getPluginManager().registerEvents(new PvPListener(teamManager), this);

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
