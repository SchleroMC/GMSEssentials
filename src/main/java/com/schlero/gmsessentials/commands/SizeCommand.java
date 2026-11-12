package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class SizeCommand implements BasicCommand {

    private final Messages messages;

    public SizeCommand(GmsePlugin plugin) {
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage(messages.get("size.usage"));
            return;
        }

        Player target;
        double scale;

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.get("size.console-usage"));
                return;
            }
            target = player;
            try {
                scale = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(messages.get("generic.invalid-number"));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(messages.get("generic.player-not-found"));
                return;
            }
            try {
                scale = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(messages.get("generic.invalid-number"));
                return;
            }
        }

        AttributeInstance attribute = target.getAttribute(Attribute.SCALE);
        if (attribute == null) {
            sender.sendMessage(messages.get("size.unsupported"));
            return;
        }

        attribute.setBaseValue(scale);
        sender.sendMessage(messages.get("size.set-other", "player", target.getName(), "scale", scale));
        if (!sender.equals(target)) {
            target.sendMessage(messages.get("size.set-self", "scale", scale));
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
