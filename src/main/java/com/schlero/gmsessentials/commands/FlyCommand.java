package com.schlero.gmsessentials.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class FlyCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
                return;
            }
            target = player;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return;
            }
        }

        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);

        if (!newState) {
            target.setFlying(false);
        }

        String status = newState ? "enabled" : "disabled";

        sender.sendMessage(Component.text("Fly " + status + " for " + target.getName() + ".", NamedTextColor.WHITE));
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("Your fly ability was " + status + ".", NamedTextColor.WHITE));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return CommandSuggestions.players(args);
        }
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "gmse.admin";
    }
}
