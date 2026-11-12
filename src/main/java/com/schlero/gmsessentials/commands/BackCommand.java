package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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

    private final GmsePlugin plugin;
    private final Messages messages;
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    public BackCommand(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();
        deathLocations.put(player.getUniqueId(), deathLocation);

        String coords = formatCoords(deathLocation);
        player.sendMessage(messages.get("back.death-message", "coords", coords));
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("generic.players-only"));
            return;
        }

        Location deathLocation = deathLocations.get(player.getUniqueId());
        if (deathLocation == null) {
            player.sendMessage(messages.get("back.no-death-location"));
            return;
        }

        player.teleportAsync(deathLocation).thenAccept(success -> {
            if (success) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(messages.get("back.teleported"));
            } else {
                player.sendMessage(messages.get("back.teleport-failed"));
            }
        });
    }

    @Override
    public String permission() {
        return "gmse.back";
    }

    private String formatCoords(Location location) {
        return String.format("%.0f, %.0f, %.0f", location.getX(), location.getY(), location.getZ());
    }
}
