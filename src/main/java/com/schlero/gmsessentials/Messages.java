package com.schlero.gmsessentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads every player-facing message from config.yml (under the "messages" section),
 * falling back to a built-in default if the key is missing. Supports '&' color codes
 * and simple {placeholder} substitution.
 */
public class Messages {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();

    private final GmsePlugin plugin;
    private final Map<String, String> defaults = new LinkedHashMap<>();

    public Messages(GmsePlugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    private void registerDefaults() {
        // Generic
        defaults.put("generic.players-only", "&cOnly players can use this command.");
        defaults.put("generic.console-must-specify-player", "&cConsole must specify a player.");
        defaults.put("generic.player-not-found", "&cPlayer not found.");
        defaults.put("generic.invalid-number", "&cInvalid number format.");

        // Gamemode
        defaults.put("gamemode.set-other", "Set {player}'s gamemode to {mode}.");
        defaults.put("gamemode.set-self", "Your game mode was updated to {mode}.");

        // Size
        defaults.put("size.usage", "&cUsage: /size [player] <scale> OR /size <scale>");
        defaults.put("size.console-usage", "&cConsole must specify a player: /size <player> <scale>");
        defaults.put("size.unsupported", "&cScale attribute is not supported on this entity.");
        defaults.put("size.set-other", "Set {player}'s scale to {scale}");
        defaults.put("size.set-self", "Your scale was set to {scale}");

        // Fly
        defaults.put("fly.enabled-other", "Fly enabled for {player}.");
        defaults.put("fly.disabled-other", "Fly disabled for {player}.");
        defaults.put("fly.enabled-self", "Your fly ability was enabled.");
        defaults.put("fly.disabled-self", "Your fly ability was disabled.");

        // Home
        defaults.put("home.players-only", "&cOnly players can use home commands.");
        defaults.put("home.saved", "Home '{name}' saved successfully.");
        defaults.put("home.not-set", "&cYou have not set a home yet! Use /sethome.");
        defaults.put("home.not-found", "&cYou have no home named '{name}'.");
        defaults.put("home.max-reached", "&cYou have reached the maximum of {max} homes. Delete one with /delhome <name> first.");
        defaults.put("home.teleported", "Teleported to home '{name}'.");
        defaults.put("home.teleport-failed", "&cCould not teleport you to that home.");
        defaults.put("home.deleted", "Deleted home '{name}'.");
        defaults.put("home.delhome-usage", "&cUsage: /delhome <name>");
        defaults.put("home.list-empty", "&cYou have no homes set. Use /sethome [name] to create one.");
        defaults.put("home.list-header", "Your homes ({count}/{max}):");
        defaults.put("home.list-entry", " - {name}");

        // Back / death
        defaults.put("back.death-message", "You died at {coords}. Type /back to teleport to your death coordinates.");
        defaults.put("back.no-death-location", "&cYou have no death location to teleport to.");
        defaults.put("back.teleported", "Teleported to your death location.");
        defaults.put("back.teleport-failed", "&cCould not teleport you to your death location.");

        // TPA system
        defaults.put("tpa.usage", "Usage: /{command} <player>");
        defaults.put("tpa.self-request", "&cYou cannot send a teleport request to yourself.");
        defaults.put("tpa.sent", "Teleport request sent to {player}.");
        defaults.put("tpa.received-tpa", "{player} has requested to teleport to you. Type /tpaccept or /tpacancel.");
        defaults.put("tpa.received-tpahere", "{player} has requested that you teleport to them. Type /tpaccept or /tpacancel.");
        defaults.put("tpa.expired-requester", "Your teleport request to {player} has expired.");
        defaults.put("tpa.expired-target", "The teleport request from {player} has expired.");
        defaults.put("tpa.none-pending", "&cYou have no pending teleport requests.");
        defaults.put("tpa.player-offline", "&cThat player is no longer online.");
        defaults.put("tpa.accepted-target", "Teleport request accepted.");
        defaults.put("tpa.accepted-requester", "{player} accepted your teleport request.");
        defaults.put("tpa.cancelled-target", "Teleport request cancelled.");
        defaults.put("tpa.cancelled-requester", "{player} cancelled your teleport request.");
        defaults.put("tpa.denied-target", "Teleport request denied.");
        defaults.put("tpa.denied-requester", "{player} denied your teleport request.");

        // /gmse
        defaults.put("gmse.usage", "Usage: /gmse reload");
        defaults.put("gmse.reloaded", "GMSEssentials configuration reloaded.");

        // Invsee
        defaults.put("invsee.players-only", "&cOnly players can use /invsee.");
        defaults.put("invsee.usage", "&cUsage: /invsee <player>");
        defaults.put("invsee.cannot-invsee-self", "&cYou cannot invsee yourself.");
        defaults.put("invsee.target-logged-off", "&cTarget player logged off.");
    }

    /**
     * Resolves a message by key, applying {placeholder} substitutions, and returns
     * it as a ready-to-send Component. Falls back to the built-in default if the
     * key is absent from config.yml.
     */
    public Component get(String key, Object... placeholders) {
        String raw = plugin.getConfig().getString("messages." + key, defaults.get(key));
        if (raw == null) {
            raw = "";
        }

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }

        return LEGACY_AMPERSAND.deserialize(raw);
    }

    /**
     * Returns every default key/value pair, used to populate config.yml documentation
     * and for reference. Not used at runtime.
     */
    public Map<String, String> getDefaults() {
        return defaults;
    }
}
