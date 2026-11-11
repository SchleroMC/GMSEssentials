package com.schlero.gmsessentials;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stores player homes in a dedicated homes.yml file instead of the main config.yml.
 */
public class HomeManager {

    private static final String HOMES_FILE = "homes.yml";

    private final GmsePlugin plugin;
    private final File homesFile;
    private FileConfiguration homesConfig;

    public HomeManager(GmsePlugin plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), HOMES_FILE);
        reload();
    }

    public Location getHome(UUID playerId) {
        return homesConfig.getLocation(playerId.toString());
    }

    public void setHome(UUID playerId, Location location) {
        homesConfig.set(playerId.toString(), location);
        save();
    }

    public void reload() {
        if (!homesFile.exists()) {
            plugin.saveResource(HOMES_FILE, false);
        }

        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        migrateLegacyHomes();
    }

    public void save() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + HOMES_FILE, e);
        }
    }

    /**
     * Moves homes that were previously stored under "homes" in config.yml.
     */
    private void migrateLegacyHomes() {
        ConfigurationSection legacyHomes = plugin.getConfig().getConfigurationSection("homes");
        if (legacyHomes == null || legacyHomes.getKeys(false).isEmpty()) {
            return;
        }

        for (String playerId : legacyHomes.getKeys(false)) {
            if (!homesConfig.isSet(playerId)) {
                homesConfig.set(playerId, legacyHomes.get(playerId));
            }
        }
        save();

        plugin.getConfig().set("homes", null);
        plugin.saveConfig();
        plugin.getLogger().info("Migrated saved homes from config.yml to " + HOMES_FILE + ".");
    }
}
