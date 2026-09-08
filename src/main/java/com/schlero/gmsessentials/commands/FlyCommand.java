package com.schlero.gmsessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player.");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        }

        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);

        if (!newState) {
            target.setFlying(false);
        }

        String status = newState ? "enabled" : "disabled";

        sender.sendMessage(ChatColor.WHITE + "Fly " + status + " for " + target.getName() + ".");
        if (sender != target) {
            target.sendMessage(ChatColor.WHITE + "Your fly ability was " + status + ".");
        }

        return true;
    }
}
