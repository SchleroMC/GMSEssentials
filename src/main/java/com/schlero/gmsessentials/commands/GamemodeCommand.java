package com.schlero.gmsessentials.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class GamemodeCommand implements BasicCommand {

    private final GameMode mode;
    private final String modeName;

    public GamemodeCommand(GameMode mode, String modeName) {
        this.mode = mode;
        this.modeName = modeName;
    }

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

        target.setGameMode(mode);
        sender.sendMessage(Component.text("Set " + target.getName() + "'s gamemode to " + modeName + ".", NamedTextColor.WHITE));
        if (!sender.equals(target)) {
            target.sendMessage(Component.text("Your game mode was updated to " + modeName + ".", NamedTextColor.WHITE));
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
