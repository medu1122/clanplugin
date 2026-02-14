package me.skibidi.clancore;

import me.skibidi.clancore.chat.ClanChatManager;
import me.skibidi.clancore.chat.TeamChatManager;
import me.skibidi.clancore.clan.BuffManager;
import me.skibidi.clancore.clan.ClanManager;
import me.skibidi.clancore.clan.ClanPointManager;
import me.skibidi.clancore.commands.ClanAdminCommand;
import me.skibidi.clancore.commands.ClanAdminTabCompleter;
import me.skibidi.clancore.commands.ClanCommand;
import me.skibidi.clancore.commands.ClanTabCompleter;
import me.skibidi.clancore.commands.TeamAdminCommand;
import me.skibidi.clancore.commands.TeamAdminTabCompleter;
import me.skibidi.clancore.commands.TeamCommand;
import me.skibidi.clancore.commands.TeamTabCompleter;
import me.skibidi.clancore.config.ConfigManager;
import me.skibidi.clancore.esp.EspManager;
import me.skibidi.clancore.listeners.GUIListener;
import me.skibidi.clancore.listeners.JoinListener;
import me.skibidi.clancore.listeners.PvPListener;
import me.skibidi.clancore.listeners.QuitListener;
import me.skibidi.clancore.storage.DatabaseManager;
import me.skibidi.clancore.storage.SQLiteStorage;
import me.skibidi.clancore.storage.repository.ClanMemberRepository;
import me.skibidi.clancore.storage.repository.ClanRepository;
import me.skibidi.clancore.team.TeamManager;
import me.skibidi.clancore.war.WarManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanCorePlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private SQLiteStorage sqliteStorage;
    private ClanRepository clanRepository;
    private ClanMemberRepository clanMemberRepository;
    private ConfigManager configManager;
    private ClanManager clanManager;
    private TeamManager teamManager;
    private WarManager warManager;
    private EspManager espManager;
    private ClanChatManager clanChatManager;
    private TeamChatManager teamChatManager;
    private ClanPointManager pointManager;
    private BuffManager buffManager;

    @Override
    public void onEnable() {
        try {
            // Database + storage
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            sqliteStorage = new SQLiteStorage(databaseManager);
            sqliteStorage.initTables();

            // Config
            configManager = new ConfigManager(this);

            // Repositories
            clanRepository = new ClanRepository(databaseManager);
            clanMemberRepository = new ClanMemberRepository(databaseManager);

            // Managers (ClanManager sẽ load từ DB sau khi set ConfigManager)
            clanManager = new ClanManager(clanRepository, clanMemberRepository);
            clanManager.setConfigManager(configManager); // Set config manager and trigger DB load
            teamManager = new TeamManager();
            warManager = new WarManager();
            espManager = new EspManager(clanManager, teamManager, warManager);
            clanChatManager = new ClanChatManager(clanManager);
            teamChatManager = new TeamChatManager(teamManager);
            pointManager = new ClanPointManager(clanManager, configManager);
            buffManager = new BuffManager(clanManager, pointManager);

            // Wire BuffManager vào ClanManager để tự động apply buffs khi level up
            clanManager.setBuffManager(buffManager);

            // Commands
            getCommand("clan").setExecutor(new ClanCommand(clanManager, warManager, espManager, clanChatManager, configManager, pointManager, buffManager));
            getCommand("clan").setTabCompleter(new ClanTabCompleter(clanManager));
            getCommand("team").setExecutor(new TeamCommand(teamManager, teamChatManager, espManager));
            getCommand("team").setTabCompleter(new TeamTabCompleter());
            getCommand("clanadmin").setExecutor(new ClanAdminCommand(clanManager, pointManager, configManager));
            getCommand("clanadmin").setTabCompleter(new ClanAdminTabCompleter(clanManager));
            getCommand("teamadmin").setExecutor(new TeamAdminCommand(teamManager));
            getCommand("teamadmin").setTabCompleter(new TeamAdminTabCompleter());

            // Listeners
            getServer().getPluginManager().registerEvents(new JoinListener(espManager, buffManager, this), this);
            getServer().getPluginManager().registerEvents(new QuitListener(teamManager, espManager, buffManager), this);
            getServer().getPluginManager().registerEvents(new PvPListener(teamManager), this);
            getServer().getPluginManager().registerEvents(new GUIListener(clanManager, teamManager, pointManager, configManager, this), this);

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
