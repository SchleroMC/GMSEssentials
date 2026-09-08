package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SizeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /size [player] <scale> OR /size <scale>");
            return true;
        }

        Player target = null;
        double scale = 1.0;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /size <player> <scale>");
                return true;
            }
            target = (Player) sender;
            try {
                scale = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format.");
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            try {
                scale = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format.");
                return true;
            }
        }

        AttributeInstance attribute = target.getAttribute(Attribute.GENERIC_SCALE);
        if (attribute != null) {
            attribute.setBaseValue(scale);
            sender.sendMessage(ChatColor.WHITE + "Set " + target.getName() + "'s scale to " + scale);
            if (sender != target) {
                target.sendMessage(ChatColor.WHITE + "Your scale was set to " + scale);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Scale attribute is not supported on this entity.");
        }

        return true;
    }
}
