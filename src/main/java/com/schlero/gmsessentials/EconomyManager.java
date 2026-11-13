package com.schlero.gmsessentials;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

/**
 * Hooks into Vault's Economy service, if Vault is installed. Vault is declared as a
 * dependency with join-classpath enabled (see paper-plugin.yml), so if it's present it
 * will have already loaded by the time this runs in onEnable().
 *
 * If another economy plugin (EssentialsX, CMI, etc.) has already registered an Economy
 * provider with Vault, that one is used. Otherwise, as long as "economy.enabled" is true
 * in config.yml, GMSEssentials registers its own lightweight provider (GmseEconomy) so
 * commands like /balance and /pay still work out of the box.
 *
 * Economy-dependent commands should check isEnabled() before using getEconomy(), since
 * Vault itself is always optional.
 */
public class EconomyManager {

    private final GmsePlugin plugin;
    private Economy economy;
    private GmseEconomy ownProvider;

    public EconomyManager(GmsePlugin plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found; economy features are disabled.");
            return;
        }

        try {
            RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

            if (provider == null && plugin.getConfig().getBoolean("economy.enabled", true)) {
                ownProvider = new GmseEconomy(plugin);
                plugin.getServer().getServicesManager().register(Economy.class, ownProvider, plugin, ServicePriority.Normal);
                provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            }

            if (provider == null) {
                plugin.getLogger().warning("Vault is installed, but no economy plugin is registered with it. Economy features are disabled.");
                return;
            }

            economy = provider.getProvider();
            plugin.getLogger().info("Hooked into Vault economy provider: " + economy.getName());
        } catch (Throwable t) {
            // Guards against classloader edge cases (e.g. missing join-classpath on an
            // older Paper build) so a broken Vault hook never takes the whole plugin down.
            plugin.getLogger().warning("Could not hook into Vault's economy service; economy features are disabled. (" + t + ")");
        }
    }

    /**
     * True only if Vault is installed, an economy plugin is registered with it,
     * and that provider reports itself as enabled.
     */
    public boolean isEnabled() {
        return economy != null && economy.isEnabled();
    }

    /**
     * May be null if isEnabled() is false. Always check isEnabled() first.
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Re-reads balances.yml from disk, if GMSEssentials is the one providing the economy
     * (i.e. no other economy plugin took over). Safe to call even if it isn't; does
     * nothing in that case. Called from /gmse reload.
     */
    public void reload() {
        if (ownProvider != null) {
            ownProvider.reload();
        }
    }

    /**
     * Persists balances to disk, if GMSEssentials is the one providing the economy
     * (i.e. no other economy plugin took over). Safe to call even if it isn't; does
     * nothing in that case. Called from GmsePlugin#onDisable().
     */
    public void save() {
        if (ownProvider != null) {
            ownProvider.save();
        }
    }
}
