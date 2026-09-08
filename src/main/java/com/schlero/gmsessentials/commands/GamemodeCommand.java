package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {

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

        GameMode mode;
        String modeName;

        switch (command.getName().toLowerCase()) {
            case "gmc":
                mode = GameMode.CREATIVE;
                modeName = "Creative";
                break;
            case "gma":
                mode = GameMode.ADVENTURE;
                modeName = "Adventure";
                break;
            case "gmsp":
                mode = GameMode.SPECTATOR;
                modeName = "Spectator";
                break;
            case "gms":
            default:
                mode = GameMode.SURVIVAL;
                modeName = "Survival";
                break;
        }

        target.setGameMode(mode);
        sender.sendMessage(ChatColor.WHITE + "Set " + target.getName() + "'s gamemode to " + modeName + ".");
        if (sender != target) {
            target.sendMessage(ChatColor.WHITE + "Your game mode was updated to " + modeName + ".");
        }

        return true;
    }
}
