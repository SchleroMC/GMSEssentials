package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.EconomyManager;
import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

/**
 * Houses every economy-facing command (/balance, /pay, /eco). All of them operate
 * through whatever Economy provider is currently hooked via Vault (see EconomyManager),
 * whether that's GMSEssentials' own GmseEconomy or a third-party economy plugin.
 */
public class EconomyCommands {

    private final GmsePlugin plugin;
    private final Messages messages;

    public EconomyCommands(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    public BasicCommand balanceCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                handleBalance(source.getSender(), args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                if (args.length <= 1 && source.getSender().hasPermission("gmse.admin")) {
                    return CommandSuggestions.players(args);
                }
                return Collections.emptyList();
            }

            @Override
            public String permission() {
                return "gmse.balance";
            }
        };
    }

    public BasicCommand payCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                handlePay(source.getSender(), args);
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
                return "gmse.pay";
            }
        };
    }

    public BasicCommand ecoCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                handleEco(source.getSender(), args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                if (args.length <= 1) {
                    return CommandSuggestions.options(args, "give", "take", "set");
                }
                if (args.length == 2) {
                    return CommandSuggestions.players(args);
                }
                return Collections.emptyList();
            }

            @Override
            public String permission() {
                return "gmse.admin";
            }
        };
    }

    private void handleBalance(CommandSender sender, String[] args) {
        EconomyManager economyManager = plugin.getEconomyManager();
        if (!economyManager.isEnabled()) {
            sender.sendMessage(messages.get("economy.disabled"));
            return;
        }
        Economy economy = economyManager.getEconomy();

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.get("generic.console-must-specify-player"));
                return;
            }
            sender.sendMessage(messages.get("economy.balance-self", "amount", economy.format(economy.getBalance(player))));
            return;
        }

        if (!sender.hasPermission("gmse.admin")) {
            sender.sendMessage(messages.get("generic.no-permission"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(messages.get("generic.player-not-found"));
            return;
        }

        sender.sendMessage(messages.get("economy.balance-other", "player", target.getName(), "amount", economy.format(economy.getBalance(target))));
    }

    private void handlePay(CommandSender sender, String[] args) {
        EconomyManager economyManager = plugin.getEconomyManager();
        if (!economyManager.isEnabled()) {
            sender.sendMessage(messages.get("economy.disabled"));
            return;
        }

        if (!(sender instanceof Player payer)) {
            sender.sendMessage(messages.get("generic.players-only"));
            return;
        }

        if (args.length < 2) {
            payer.sendMessage(messages.get("economy.pay-usage"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            payer.sendMessage(messages.get("generic.player-not-found"));
            return;
        }

        if (target.equals(payer)) {
            payer.sendMessage(messages.get("economy.pay-self"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            payer.sendMessage(messages.get("generic.invalid-number"));
            return;
        }

        if (amount <= 0) {
            payer.sendMessage(messages.get("economy.pay-invalid-amount"));
            return;
        }

        Economy economy = economyManager.getEconomy();
        if (!economy.has(payer, amount)) {
            payer.sendMessage(messages.get("economy.pay-insufficient-funds", "amount", economy.format(amount)));
            return;
        }

        EconomyResponse withdrawal = economy.withdrawPlayer(payer, amount);
        if (!withdrawal.transactionSuccess()) {
            payer.sendMessage(messages.get("economy.pay-insufficient-funds", "amount", economy.format(amount)));
            return;
        }

        EconomyResponse deposit = economy.depositPlayer(target, amount);
        if (!deposit.transactionSuccess()) {
            // Refund the payer if the deposit side failed for any reason, so money isn't lost.
            economy.depositPlayer(payer, amount);
            payer.sendMessage(messages.get("economy.pay-usage"));
            return;
        }

        payer.sendMessage(messages.get("economy.pay-sent", "player", target.getName(), "amount", economy.format(amount)));
        target.sendMessage(messages.get("economy.pay-received", "player", payer.getName(), "amount", economy.format(amount)));
    }

    private void handleEco(CommandSender sender, String[] args) {
        EconomyManager economyManager = plugin.getEconomyManager();
        if (!economyManager.isEnabled()) {
            sender.sendMessage(messages.get("economy.disabled"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(messages.get("economy.eco-usage"));
            return;
        }

        String action = args[0].toLowerCase();
        if (!action.equals("give") && !action.equals("take") && !action.equals("set")) {
            sender.sendMessage(messages.get("economy.eco-usage"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(messages.get("generic.player-not-found"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("generic.invalid-number"));
            return;
        }

        if (amount < 0) {
            sender.sendMessage(messages.get("economy.eco-invalid-amount"));
            return;
        }

        Economy economy = economyManager.getEconomy();

        switch (action) {
            case "give" -> {
                EconomyResponse response = economy.depositPlayer(target, amount);
                sender.sendMessage(messages.get("economy.eco-given", "player", target.getName(),
                        "amount", economy.format(amount), "balance", economy.format(response.balance)));
            }
            case "take" -> {
                EconomyResponse response = economy.withdrawPlayer(target, amount);
                double newBalance = response.transactionSuccess() ? response.balance : economy.getBalance(target);
                sender.sendMessage(messages.get("economy.eco-taken", "player", target.getName(),
                        "amount", economy.format(amount), "balance", economy.format(newBalance)));
            }
            case "set" -> {
                double current = economy.getBalance(target);
                double diff = amount - current;
                if (diff > 0) {
                    economy.depositPlayer(target, diff);
                } else if (diff < 0) {
                    economy.withdrawPlayer(target, -diff);
                }
                sender.sendMessage(messages.get("economy.eco-set", "player", target.getName(), "amount", economy.format(amount)));
            }
        }
    }
}
