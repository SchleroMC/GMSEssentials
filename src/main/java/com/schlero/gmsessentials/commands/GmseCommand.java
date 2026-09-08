package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GmseCommand implements CommandExecutor {

    private final GmsePlugin plugin;

    public GmseCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.WHITE + "Usage: /gmse reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.WHITE + "GMSEssentials configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.WHITE + "Usage: /gmse reload");
        return true;
    }
}
