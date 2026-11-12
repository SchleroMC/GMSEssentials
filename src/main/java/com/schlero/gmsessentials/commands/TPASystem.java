package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
        CANCEL,
        DENY
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
    private final Messages messages;
    // Key: target's UUID (the player who must accept), Value: incoming request
    private final Map<UUID, Request> pendingRequests = new HashMap<>();
    private static final long TIMEOUT_SECONDS = 60L;

    public TPASystem(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
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

            @Override
            public String permission() {
                return "gmse.tpa";
            }
        };
    }

    public void handle(CommandSourceStack source, String[] args, Action action) {
        CommandSender sender = source.getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("generic.players-only"));
            return;
        }

        switch (action) {
            case TPA -> handleTpa(player, args, RequestType.TPA);
            case TPAHERE -> handleTpa(player, args, RequestType.TPAHERE);
            case ACCEPT -> handleAccept(player);
            case CANCEL -> handleCancelOrDeny(player, false);
            case DENY -> handleCancelOrDeny(player, true);
        }
    }

    private void handleTpa(Player requester, String[] args, RequestType type) {
        if (args.length < 1) {
            requester.sendMessage(messages.get("tpa.usage", "command", type == RequestType.TPA ? "tpa" : "tpahere"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            requester.sendMessage(messages.get("generic.player-not-found"));
            return;
        }

        if (target.equals(requester)) {
            requester.sendMessage(messages.get("tpa.self-request"));
            return;
        }

        // Cancel any existing request already pending on this target.
        clearRequest(target.getUniqueId());

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(target.getUniqueId());
            requester.sendMessage(messages.get("tpa.expired-requester", "player", target.getName()));
            target.sendMessage(messages.get("tpa.expired-target", "player", requester.getName()));
        }, TIMEOUT_SECONDS * 20L);

        pendingRequests.put(target.getUniqueId(), new Request(requester.getUniqueId(), type, timeoutTask));

        requester.sendMessage(messages.get("tpa.sent", "player", target.getName()));
        if (type == RequestType.TPA) {
            target.sendMessage(messages.get("tpa.received-tpa", "player", requester.getName()));
        } else {
            target.sendMessage(messages.get("tpa.received-tpahere", "player", requester.getName()));
        }
    }

    private void handleAccept(Player target) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(messages.get("tpa.none-pending"));
            return;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        request.timeoutTask.cancel();
        pendingRequests.remove(target.getUniqueId());

        if (requester == null || !requester.isOnline()) {
            target.sendMessage(messages.get("tpa.player-offline"));
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

        target.sendMessage(messages.get("tpa.accepted-target"));
        requester.sendMessage(messages.get("tpa.accepted-requester", "player", target.getName()));
    }

    /**
     * Handles both /tpacancel and /tpdeny. Both are run by the player who received the
     * request and simply discard it; the only difference is the wording of the message
     * each side gets ("cancelled" vs. "denied").
     */
    private void handleCancelOrDeny(Player target, boolean deny) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(messages.get("tpa.none-pending"));
            return;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        clearRequest(target.getUniqueId());

        target.sendMessage(messages.get(deny ? "tpa.denied-target" : "tpa.cancelled-target"));
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(messages.get(deny ? "tpa.denied-requester" : "tpa.cancelled-requester", "player", target.getName()));
        }
    }

    private void clearRequest(UUID targetId) {
        Request existing = pendingRequests.remove(targetId);
        if (existing != null) {
            existing.timeoutTask.cancel();
        }
    }
}
