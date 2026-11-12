package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class FlyCommand implements BasicCommand {

    private final Messages messages;

    public FlyCommand(GmsePlugin plugin) {
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.get("generic.console-must-specify-player"));
                return;
            }
            target = player;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(messages.get("generic.player-not-found"));
                return;
            }
        }

        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);

        if (!newState) {
            target.setFlying(false);
        }

        sender.sendMessage(messages.get(newState ? "fly.enabled-other" : "fly.disabled-other", "player", target.getName()));
        if (!sender.equals(target)) {
            target.sendMessage(messages.get(newState ? "fly.enabled-self" : "fly.disabled-self"));
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
