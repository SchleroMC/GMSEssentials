package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackCommand implements CommandExecutor, Listener {

    private final GmsePlugin plugin;
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    public BackCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Location deathLoc = p.getLocation();
        deathLocations.put(p.getUniqueId(), deathLoc);

        String coords = formatCoords(deathLoc);

        String message = plugin.getConfig().getString(
                "messages.death-message",
                "You died at {coords}. Type /back to teleport to your death coordinates."
        );

        message = ChatColor.translateAlternateColorCodes('&', message.replace("{coords}", coords));
        p.sendMessage(message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;
        Location deathLoc = deathLocations.get(p.getUniqueId());

        if (deathLoc == null) {
            p.sendMessage(ChatColor.RED + "You have no death location to teleport to.");
            return true;
        }

        p.teleport(deathLoc);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        p.sendMessage(ChatColor.WHITE + "Teleported to your death location.");

        return true;
    }

    private String formatCoords(Location loc) {
        return String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }
}
