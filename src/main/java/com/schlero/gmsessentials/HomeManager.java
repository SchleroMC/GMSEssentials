package com.schlero.gmsessentials;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stores player homes in a dedicated homes.yml file instead of the main config.yml.
 * Each player may have multiple named homes, up to the "homes.max-homes" limit in config.yml.
 */
public class HomeManager {

    public static final String DEFAULT_HOME_NAME = "home";
    private static final String HOMES_FILE = "homes.yml";

    private final GmsePlugin plugin;
    private final File homesFile;
    private FileConfiguration homesConfig;

    public HomeManager(GmsePlugin plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), HOMES_FILE);
        reload();
    }

    public int getMaxHomes() {
        return plugin.getConfig().getInt("homes.max-homes", 3);
    }

    public Location getHome(UUID playerId, String name) {
        ConfigurationSection section = homesConfig.getConfigurationSection(playerId.toString());
        if (section == null) {
            return null;
        }
        return section.getLocation(name.toLowerCase());
    }

    /**
     * Returns an unmodifiable, name-ordered map of every home the player has saved.
     */
    public Map<String, Location> getHomes(UUID playerId) {
        ConfigurationSection section = homesConfig.getConfigurationSection(playerId.toString());
        Map<String, Location> homes = new LinkedHashMap<>();
        if (section == null) {
            return homes;
        }

        for (String name : section.getKeys(false)) {
            Location location = section.getLocation(name);
            if (location != null) {
                homes.put(name, location);
            }
        }

        return Collections.unmodifiableMap(homes);
    }

    public int getHomeCount(UUID playerId) {
        return getHomes(playerId).size();
    }

    public boolean hasHome(UUID playerId, String name) {
        return getHome(playerId, name) != null;
    }

    /**
     * Saves a home under the given name. Returns false (without saving) if the player
     * is at their max-homes limit and does not already have a home with this name.
     */
    public boolean setHome(UUID playerId, String name, Location location) {
        String key = name.toLowerCase();

        if (!hasHome(playerId, key) && getHomeCount(playerId) >= getMaxHomes()) {
            return false;
        }

        homesConfig.set(playerId.toString() + "." + key, location);
        save();
        return true;
    }

    /**
     * Deletes a named home. Returns false if the player had no home by that name.
     */
    public boolean deleteHome(UUID playerId, String name) {
        String key = name.toLowerCase();
        if (!hasHome(playerId, key)) {
            return false;
        }

        homesConfig.set(playerId.toString() + "." + key, null);
        save();
        return true;
    }

    public void reload() {
        if (!homesFile.exists()) {
            plugin.saveResource(HOMES_FILE, false);
        }

        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        migrateLegacyHomes();
        migrateSingleHomes();
    }

    public void save() {
        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + HOMES_FILE, e);
        }
    }

    /**
     * Moves homes that were previously stored under "homes" in config.yml, back when
     * homes.yml didn't exist yet. Skips the "max-homes" setting, which lives in the
     * same "homes" section of config.yml but is not a player home. Only touches
     * (and re-saves) config.yml if an actual legacy home entry is found, so the
     * file's comments aren't stripped on every normal startup.
     */
    private void migrateLegacyHomes() {
        ConfigurationSection legacyHomes = plugin.getConfig().getConfigurationSection("homes");
        if (legacyHomes == null) {
            return;
        }

        boolean foundLegacyEntry = false;
        for (String playerId : legacyHomes.getKeys(false)) {
            if (playerId.equalsIgnoreCase("max-homes")) {
                continue;
            }

            foundLegacyEntry = true;
            if (!homesConfig.isSet(playerId)) {
                homesConfig.set(playerId + "." + DEFAULT_HOME_NAME, legacyHomes.getLocation(playerId));
            }
            plugin.getConfig().set("homes." + playerId, null);
        }

        if (foundLegacyEntry) {
            save();
            plugin.saveConfig();
            plugin.getLogger().info("Migrated saved homes from config.yml to " + HOMES_FILE + ".");
        }
    }

    /**
     * Upgrades homes.yml entries from the old single-home format (<uuid>: <location>)
     * to the new named-home format (<uuid>.<name>: <location>).
     */
    private void migrateSingleHomes() {
        boolean migratedAny = false;

        for (String playerId : homesConfig.getKeys(false)) {
            if (homesConfig.isConfigurationSection(playerId)) {
                continue;
            }

            Location legacyHome = homesConfig.getLocation(playerId);
            if (legacyHome == null) {
                continue;
            }

            homesConfig.set(playerId, null);
            homesConfig.set(playerId + "." + DEFAULT_HOME_NAME, legacyHome);
            migratedAny = true;
        }

        if (migratedAny) {
            save();
            plugin.getLogger().info("Migrated single-home entries in " + HOMES_FILE + " to the named-home format.");
        }
    }
}
