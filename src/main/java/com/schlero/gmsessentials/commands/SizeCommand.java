package com.schlero.gmsessentials.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class SizeCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /size [player] <scale> OR /size <scale>", NamedTextColor.RED));
            return;
        }

        Player target;
        double scale;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Console must specify a player: /size <player> <scale>", NamedTextColor.RED));
                return;
            }
            target = player;
            try {
                scale = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid number format.", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return;
            }
            try {
                scale = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid number format.", NamedTextColor.RED));
                return;
            }
        }

        AttributeInstance attribute = target.getAttribute(Attribute.SCALE);
        if (attribute == null) {
            sender.sendMessage(Component.text("Scale attribute is not supported on this entity.", NamedTextColor.RED));
            return;
        }

        attribute.setBaseValue(scale);
        sender.sendMessage(Component.text("Set " + target.getName() + "'s scale to " + scale, NamedTextColor.WHITE));
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("Your scale was set to " + scale, NamedTextColor.WHITE));
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
