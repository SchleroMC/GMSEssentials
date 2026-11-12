package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class WorkbenchCommand implements BasicCommand {

    private final Messages messages;

    public WorkbenchCommand(GmsePlugin plugin) {
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("generic.players-only"));
            return;
        }

        player.openWorkbench(null, true);
    }

    @Override
    public String permission() {
        return "gmse.workbench";
    }
}
