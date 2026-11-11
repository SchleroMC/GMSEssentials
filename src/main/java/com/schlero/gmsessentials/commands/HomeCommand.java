package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HomeCommand {

    private final GmsePlugin plugin;

    public HomeCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    public void setHome(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Only players can use home commands.", NamedTextColor.RED));
            return;
        }

        plugin.getHomeManager().setHome(player.getUniqueId(), player.getLocation());
        player.sendMessage(Component.text("Home saved successfully.", NamedTextColor.WHITE));
    }

    public void teleportHome(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(Component.text("Only players can use home commands.", NamedTextColor.RED));
            return;
        }

        Location homeLocation = plugin.getHomeManager().getHome(player.getUniqueId());
        if (homeLocation == null) {
            player.sendMessage(Component.text("You have not set a home yet! Use /sethome.", NamedTextColor.RED));
            return;
        }

        player.teleportAsync(homeLocation).thenAccept(success -> {
            if (success) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(Component.text("Teleported to your home.", NamedTextColor.WHITE));
            } else {
                player.sendMessage(Component.text("Could not teleport you to your home.", NamedTextColor.RED));
            }
        });
    }
}
