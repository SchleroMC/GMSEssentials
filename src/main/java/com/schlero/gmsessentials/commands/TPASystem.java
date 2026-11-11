package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPASystem {

    public enum Action {
        TPA,
        TPAHERE,
        ACCEPT,
        CANCEL
    }

    private enum RequestType {
        TPA,     // requester wants to teleport to target
        TPAHERE  // requester wants target to teleport to them
    }

    private static class Request {
        final UUID requesterId;
        final RequestType type;
        final BukkitTask timeoutTask;

        Request(UUID requesterId, RequestType type, BukkitTask timeoutTask) {
            this.requesterId = requesterId;
            this.type = type;
            this.timeoutTask = timeoutTask;
        }
    }

    private final GmsePlugin plugin;
    // Key: target's UUID (the player who must accept), Value: incoming request
    private final Map<UUID, Request> pendingRequests = new HashMap<>();
    private static final long TIMEOUT_SECONDS = 60L;

    public TPASystem(GmsePlugin plugin) {
        this.plugin = plugin;
    }

    public BasicCommand command(Action action) {
        return new BasicCommand() {
            @Override
            public void execute(CommandSourceStack source, String[] args) {
                handle(source, args, action);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack source, String[] args) {
                if ((action == Action.TPA || action == Action.TPAHERE) && args.length <= 1) {
                    return CommandSuggestions.players(args);
                }
                return Collections.emptyList();
            }
        };
    }

    public void handle(CommandSourceStack source, String[] args, Action action) {
        CommandSender sender = source.getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return;
        }

        switch (action) {
            case TPA -> handleTpa(player, args, RequestType.TPA);
            case TPAHERE -> handleTpa(player, args, RequestType.TPAHERE);
            case ACCEPT -> handleAccept(player);
            case CANCEL -> handleCancel(player);
        }
    }

    private void handleTpa(Player requester, String[] args, RequestType type) {
        if (args.length < 1) {
            requester.sendMessage(Component.text("Usage: /" + (type == RequestType.TPA ? "tpa" : "tpahere") + " <player>", NamedTextColor.WHITE));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            requester.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
            return;
        }

        if (target.equals(requester)) {
            requester.sendMessage(Component.text("You cannot send a teleport request to yourself.", NamedTextColor.RED));
            return;
        }

        // Cancel any existing request already pending on this target.
        clearRequest(target.getUniqueId());

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(target.getUniqueId());
            requester.sendMessage(Component.text("Your teleport request to " + target.getName() + " has expired.", NamedTextColor.WHITE));
            target.sendMessage(Component.text("The teleport request from " + requester.getName() + " has expired.", NamedTextColor.WHITE));
        }, TIMEOUT_SECONDS * 20L);

        pendingRequests.put(target.getUniqueId(), new Request(requester.getUniqueId(), type, timeoutTask));

        requester.sendMessage(Component.text("Teleport request sent to " + target.getName() + ".", NamedTextColor.WHITE));
        if (type == RequestType.TPA) {
            target.sendMessage(Component.text(requester.getName() + " has requested to teleport to you. Type /tpaccept or /tpacancel.", NamedTextColor.WHITE));
        } else {
            target.sendMessage(Component.text(requester.getName() + " has requested that you teleport to them. Type /tpaccept or /tpacancel.", NamedTextColor.WHITE));
        }
    }

    private void handleAccept(Player target) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(Component.text("You have no pending teleport requests.", NamedTextColor.RED));
            return;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        request.timeoutTask.cancel();
        pendingRequests.remove(target.getUniqueId());

        if (requester == null || !requester.isOnline()) {
            target.sendMessage(Component.text("That player is no longer online.", NamedTextColor.RED));
            return;
        }

        if (request.type == RequestType.TPA) {
            requester.teleportAsync(target.getLocation()).thenAccept(success -> {
                if (success) {
                    requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            });
        } else {
            target.teleportAsync(requester.getLocation()).thenAccept(success -> {
                if (success) {
                    target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            });
        }

        target.sendMessage(Component.text("Teleport request accepted.", NamedTextColor.WHITE));
        requester.sendMessage(Component.text(target.getName() + " accepted your teleport request.", NamedTextColor.WHITE));
    }

    private void handleCancel(Player target) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(Component.text("You have no pending teleport requests.", NamedTextColor.RED));
            return;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        clearRequest(target.getUniqueId());

        target.sendMessage(Component.text("Teleport request cancelled.", NamedTextColor.WHITE));
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(Component.text(target.getName() + " cancelled your teleport request.", NamedTextColor.WHITE));
        }
    }

    private void clearRequest(UUID targetId) {
        Request existing = pendingRequests.remove(targetId);
        if (existing != null) {
            existing.timeoutTask.cancel();
        }
    }
}
