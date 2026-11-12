package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Collections;

public class GmseCommand implements BasicCommand {

    private final GmsePlugin plugin;
    private final Messages messages;

    public GmseCommand(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage(messages.get("gmse.usage"));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getHomeManager().reload();
            sender.sendMessage(messages.get("gmse.reloaded"));
            return;
        }

        sender.sendMessage(messages.get("gmse.usage"));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return CommandSuggestions.options(args, "reload");
        }
        return Collections.emptyList();
    }

    @Override
    public String permission() {
        return "gmse.admin";
    }
}
