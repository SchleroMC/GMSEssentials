package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.HomeManager;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class HomeCommand {

    private final GmsePlugin plugin;
    private final Messages messages;

    public HomeCommand(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    public BasicCommand setHomeCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                setHome(source, args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                return existingHomeNames(source, args);
            }

            @Override
            public String permission() {
                return "gmse.home";
            }
        };
    }

    public BasicCommand homeCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                teleportHome(source, args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                return existingHomeNames(source, args);
            }

            @Override
            public String permission() {
                return "gmse.home";
            }
        };
    }

    public BasicCommand delHomeCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                deleteHome(source, args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                return existingHomeNames(source, args);
            }

            @Override
            public String permission() {
                return "gmse.home";
            }
        };
    }

    public BasicCommand listHomesCommand() {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                listHomes(source);
            }

            @Override
            public String permission() {
                return "gmse.home";
            }
        };
    }

    public void setHome(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("home.players-only"));
            return;
        }

        String name = args.length > 0 ? args[0] : HomeManager.DEFAULT_HOME_NAME;
        boolean saved = plugin.getHomeManager().setHome(player.getUniqueId(), name, player.getLocation());

        if (!saved) {
            player.sendMessage(messages.get("home.max-reached", "max", plugin.getHomeManager().getMaxHomes()));
            return;
        }

        player.sendMessage(messages.get("home.saved", "name", name));
    }

    public void teleportHome(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("home.players-only"));
            return;
        }

        String name = args.length > 0 ? args[0] : HomeManager.DEFAULT_HOME_NAME;
        Location homeLocation = plugin.getHomeManager().getHome(player.getUniqueId(), name);

        if (homeLocation == null) {
            if (args.length == 0) {
                player.sendMessage(messages.get("home.not-set"));
            } else {
                player.sendMessage(messages.get("home.not-found", "name", name));
            }
            return;
        }

        player.teleportAsync(homeLocation).thenAccept(success -> {
            if (success) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendMessage(messages.get("home.teleported", "name", name));
            } else {
                player.sendMessage(messages.get("home.teleport-failed"));
            }
        });
    }

    public void deleteHome(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("home.players-only"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(messages.get("home.delhome-usage"));
            return;
        }

        String name = args[0];
        boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), name);

        if (!deleted) {
            player.sendMessage(messages.get("home.not-found", "name", name));
            return;
        }

        player.sendMessage(messages.get("home.deleted", "name", name));
    }

    public void listHomes(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.get("home.players-only"));
            return;
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());

        if (homes.isEmpty()) {
            player.sendMessage(messages.get("home.list-empty"));
            return;
        }

        player.sendMessage(messages.get("home.list-header", "count", homes.size(), "max", plugin.getHomeManager().getMaxHomes()));
        for (String name : new TreeMap<>(homes).keySet()) {
            player.sendMessage(messages.get("home.list-entry", "name", name));
        }
    }

    private Collection<String> existingHomeNames(CommandSourceStack source, String[] args) {
        if (args.length > 1 || !(source.getSender() instanceof Player player)) {
            return Collections.emptyList();
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        return CommandSuggestions.options(args, homes.keySet().toArray(new String[0]));
    }
}
