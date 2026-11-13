package com.schlero.gmsessentials;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * GMSEssentials' own lightweight Vault Economy implementation. This only gets registered
 * with Vault (see EconomyManager) when no other economy plugin has already claimed the
 * Economy service, so it never conflicts with EssentialsX, CMI, etc.
 *
 * Balances are stored in balances.yml, keyed by player UUID, and tracked internally as
 * BigDecimal (rounded to 2 decimal places) to avoid the usual floating-point drift that
 * comes from doing lots of small double additions/subtractions over time.
 *
 * This intentionally does not support banks (shared multi-player accounts) or per-world
 * balances; it's meant to cover the common case of a single global currency.
 */
public class GmseEconomy implements Economy {

    private static final String BALANCES_FILE = "balances.yml";
    private static final int SCALE = 2;

    private final GmsePlugin plugin;
    private final File balancesFile;
    private FileConfiguration balancesConfig;

    public GmseEconomy(GmsePlugin plugin) {
        this.plugin = plugin;
        this.balancesFile = new File(plugin.getDataFolder(), BALANCES_FILE);
        reload();
    }

    public void reload() {
        if (!balancesFile.exists()) {
            try {
                balancesFile.getParentFile().mkdirs();
                balancesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create " + BALANCES_FILE, e);
            }
        }
        balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
    }

    public void save() {
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + BALANCES_FILE, e);
        }
    }

    private BigDecimal startingBalance() {
        return round(plugin.getConfig().getDouble("economy.starting-balance", 100.0));
    }

    private BigDecimal round(double amount) {
        return BigDecimal.valueOf(amount).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal rawBalance(UUID playerId) {
        double stored = balancesConfig.getDouble(playerId.toString(), -1);
        if (stored < 0) {
            BigDecimal starting = startingBalance();
            balancesConfig.set(playerId.toString(), starting.doubleValue());
            save();
            return starting;
        }
        return round(stored);
    }

    private void setBalance(UUID playerId, BigDecimal amount) {
        BigDecimal clamped = amount.signum() < 0 ? BigDecimal.ZERO.setScale(SCALE) : amount;
        balancesConfig.set(playerId.toString(), clamped.doubleValue());
        save();
    }

    // --- Vault Economy implementation ---

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "GMSEssentials";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return SCALE;
    }

    @Override
    public String format(double amount) {
        BigDecimal rounded = round(amount);
        return currencySymbol() + rounded.toPlainString();
    }

    private String currencySymbol() {
        return plugin.getConfig().getString("economy.currency-symbol", "$");
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("economy.currency-plural", "Dollars");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("economy.currency-singular", "Dollar");
    }

    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(resolveOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return balancesConfig.isSet(player.getUniqueId().toString());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(resolveOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return rawBalance(player.getUniqueId()).doubleValue();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(resolveOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return rawBalance(player.getUniqueId()).compareTo(round(amount)) >= 0;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(resolveOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
        }

        BigDecimal balance = rawBalance(player.getUniqueId());
        BigDecimal requested = round(amount);

        if (balance.compareTo(requested) < 0) {
            return new EconomyResponse(0, balance.doubleValue(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
        }

        BigDecimal newBalance = balance.subtract(requested);
        setBalance(player.getUniqueId(), newBalance);
        return new EconomyResponse(requested.doubleValue(), newBalance.doubleValue(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(resolveOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit a negative amount.");
        }

        BigDecimal balance = rawBalance(player.getUniqueId());
        BigDecimal deposited = round(amount);
        BigDecimal newBalance = balance.add(deposited);
        setBalance(player.getUniqueId(), newBalance);
        return new EconomyResponse(deposited.doubleValue(), newBalance.doubleValue(), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(resolveOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (hasAccount(player)) {
            return false;
        }
        setBalance(player.getUniqueId(), startingBalance());
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer resolveOfflinePlayer(String playerName) {
        return Bukkit.getOfflinePlayer(playerName);
    }

    // --- Bank accounts are not supported ---

    private static EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank accounts are not supported by GMSEssentials.");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplemented();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return notImplemented();
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }
}
