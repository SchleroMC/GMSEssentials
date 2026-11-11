package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Collections;

public class GmseCommand implements BasicCommand {

    private final GmsePlugin plugin;

    public GmseCommand(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /gmse reload", NamedTextColor.WHITE));
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getHomeManager().reload();
            sender.sendMessage(Component.text("GMSEssentials configuration reloaded.", NamedTextColor.WHITE));
            return;
        }

        sender.sendMessage(Component.text("Usage: /gmse reload", NamedTextColor.WHITE));
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
