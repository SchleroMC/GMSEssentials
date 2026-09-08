package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final GmsePlugin plugin;

    public HomeCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use home commands.");
            return true;
        }

        Player p = (Player) sender;

        if (command.getName().equalsIgnoreCase("sethome")) {
            plugin.getConfig().set("homes." + p.getUniqueId().toString(), p.getLocation());
            plugin.saveConfig();
            p.sendMessage(ChatColor.WHITE + "Home saved successfully.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("home")) {
            Location homeLoc = plugin.getConfig().getLocation("homes." + p.getUniqueId().toString());

            if (homeLoc == null) {
                p.sendMessage(ChatColor.RED + "You have not set a home yet! Use /sethome.");
                return true;
            }

            p.teleport(homeLoc);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            p.sendMessage(ChatColor.WHITE + "Teleported to your home.");
            return true;
        }

        return false;
    }
}
