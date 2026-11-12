package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class EchestCommand implements BasicCommand {

    private final Messages messages;

    public EchestCommand(GmsePlugin plugin) {
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("generic.players-only"));
            return;
        }

        player.openInventory(player.getEnderChest());
    }

    @Override
    public String permission() {
        return "gmse.echest";
    }
}
