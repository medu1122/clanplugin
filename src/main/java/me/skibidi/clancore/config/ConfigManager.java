package me.skibidi.clancore.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    // Clan points config
    private final Map<Integer, Integer> upgradeCosts = new HashMap<>();
    private final Map<Material, Integer> sellableItems = new HashMap<>();

    // Level config
    private int maxLevel = 5;
    private final Map<Integer, LevelConfig> levelConfigs = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Load upgrade costs
        upgradeCosts.clear();
        if (config.contains("clan-points.upgrade-costs")) {
            org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("clan-points.upgrade-costs");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(key);
                        int cost = config.getInt("clan-points.upgrade-costs." + key);
                        upgradeCosts.put(level, cost);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level in upgrade-costs: " + key);
                    }
                }
            } else {
                plugin.getLogger().warning("clan-points.upgrade-costs is not a valid configuration section!");
            }
        }

        // Load sellable items
        sellableItems.clear();
        if (config.contains("clan-points.sellable-items")) {
            org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("clan-points.sellable-items");
            if (section != null) {
                for (String materialName : section.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialName.toUpperCase());
                        int points = config.getInt("clan-points.sellable-items." + materialName);
                        sellableItems.put(material, points);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in config: " + materialName);
                    }
                }
            } else {
                plugin.getLogger().warning("clan-points.sellable-items is not a valid configuration section!");
            }
        }

        // Load level configs
        maxLevel = config.getInt("clan-levels.max-level", 5);
        levelConfigs.clear();
        if (config.contains("clan-levels.levels")) {
            org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("clan-levels.levels");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(key);
                        int maxMembers = config.getInt("clan-levels.levels." + level + ".max-members");
                        int speedBuff = config.getInt("clan-levels.levels." + level + ".speed-buff");
                        int healthBuff = config.getInt("clan-levels.levels." + level + ".health-buff");
                        levelConfigs.put(level, new LevelConfig(maxMembers, speedBuff, healthBuff));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid level in clan-levels.levels: " + key);
                    }
                }
            } else {
                plugin.getLogger().warning("clan-levels.levels is not a valid configuration section!");
            }
        }
    }

    public int getUpgradeCost(int currentLevel) {
        return upgradeCosts.getOrDefault(currentLevel, 0);
    }

    public boolean isSellable(Material material) {
        return sellableItems.containsKey(material);
    }

    public int getSellPrice(Material material) {
        return sellableItems.getOrDefault(material, 0);
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public LevelConfig getLevelConfig(int level) {
        return levelConfigs.get(level);
    }

    public static class LevelConfig {
        private final int maxMembers;
        private final int speedBuff;
        private final int healthBuff;

        public LevelConfig(int maxMembers, int speedBuff, int healthBuff) {
            this.maxMembers = maxMembers;
            this.speedBuff = speedBuff;
            this.healthBuff = healthBuff;
        }

        public int getMaxMembers() {
            return maxMembers;
        }

        public int getSpeedBuff() {
            return speedBuff;
        }

        public int getHealthBuff() {
            return healthBuff;
        }
    }
}
