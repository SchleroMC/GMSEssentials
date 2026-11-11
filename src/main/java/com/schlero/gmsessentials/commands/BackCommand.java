package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackCommand implements BasicCommand, Listener {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();

    private final GmsePlugin plugin;
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    public BackCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();
        deathLocations.put(player.getUniqueId(), deathLocation);

        String coords = formatCoords(deathLocation);

        String message = plugin.getConfig().getString(
                "messages.death-message",
                "You died at {coords}. Type /back to teleport to your death coordinates."
        );
        if (message == null) {
            message = "You died at {coords}. Type /back to teleport to your death coordinates.";
        }

        player.sendMessage(LEGACY_AMPERSAND.deserialize(message.replace("{coords}", coords)));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return;
        }

        Location deathLocation = deathLocations.get(player.getUniqueId());
        if (deathLocation == null) {
            player.sendMessage(Component.text("You have no death location to teleport to.", NamedTextColor.RED));
            return;
        }

        player.teleportAsync(deathLocation).thenAccept(success -> {
            if (success) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(Component.text("Teleported to your death location.", NamedTextColor.WHITE));
            } else {
                player.sendMessage(Component.text("Could not teleport you to your death location.", NamedTextColor.RED));
            }
        });
    }

    private String formatCoords(Location location) {
        return String.format("%.0f, %.0f, %.0f", location.getX(), location.getY(), location.getZ());
    }
}
