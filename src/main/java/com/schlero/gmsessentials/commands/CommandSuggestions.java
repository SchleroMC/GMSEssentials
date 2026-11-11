package com.schlero.gmsessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

final class CommandSuggestions {

    private CommandSuggestions() {
    }

    static Collection<String> players(String[] args) {
        String partial = lastArgument(args).toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(partial))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    static Collection<String> options(String[] args, String... options) {
        String partial = lastArgument(args).toLowerCase(Locale.ROOT);
        return Arrays.stream(options)
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(partial))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private static String lastArgument(String[] args) {
        return args.length == 0 ? "" : args[args.length - 1];
    }
}
