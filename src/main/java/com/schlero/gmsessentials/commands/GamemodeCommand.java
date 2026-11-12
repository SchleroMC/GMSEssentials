package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class GamemodeCommand implements BasicCommand {

    private final Messages messages;
    private final GameMode mode;
    private final String modeName;

    public GamemodeCommand(GmsePlugin plugin, GameMode mode, String modeName) {
        this.messages = plugin.getMessages();
        this.mode = mode;
        this.modeName = modeName;
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

        target.setGameMode(mode);
        sender.sendMessage(messages.get("gamemode.set-other", "player", target.getName(), "mode", modeName));
        if (!sender.equals(target)) {
            target.sendMessage(messages.get("gamemode.set-self", "mode", modeName));
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
