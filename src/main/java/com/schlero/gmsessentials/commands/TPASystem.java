package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPASystem implements CommandExecutor {

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "tpa":
                return handleTpa(player, args, RequestType.TPA);
            case "tpahere":
                return handleTpa(player, args, RequestType.TPAHERE);
            case "tpaccept":
                return handleAccept(player);
            case "tpacancel":
                return handleCancel(player);
            default:
                return false;
        }
    }

    private boolean handleTpa(Player requester, String[] args, RequestType type) {
        if (args.length < 1) {
            requester.sendMessage(ChatColor.WHITE + "Usage: /" + (type == RequestType.TPA ? "tpa" : "tpahere") + " <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            requester.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (target.equals(requester)) {
            requester.sendMessage(ChatColor.RED + "You cannot send a teleport request to yourself.");
            return true;
        }

        // Cancel any existing request already pending on this target.
        clearRequest(target.getUniqueId());

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingRequests.remove(target.getUniqueId());
            requester.sendMessage(ChatColor.WHITE + "Your teleport request to " + target.getName() + " has expired.");
            target.sendMessage(ChatColor.WHITE + "The teleport request from " + requester.getName() + " has expired.");
        }, TIMEOUT_SECONDS * 20L);

        pendingRequests.put(target.getUniqueId(), new Request(requester.getUniqueId(), type, timeoutTask));

        if (type == RequestType.TPA) {
            requester.sendMessage(ChatColor.WHITE + "Teleport request sent to " + target.getName() + ".");
            target.sendMessage(ChatColor.WHITE + requester.getName() + " has requested to teleport to you. Type /tpaccept or /tpacancel.");
        } else {
            requester.sendMessage(ChatColor.WHITE + "Teleport request sent to " + target.getName() + ".");
            target.sendMessage(ChatColor.WHITE + requester.getName() + " has requested that you teleport to them. Type /tpaccept or /tpacancel.");
        }

        return true;
    }

    private boolean handleAccept(Player target) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return true;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        request.timeoutTask.cancel();
        pendingRequests.remove(target.getUniqueId());

        if (requester == null || !requester.isOnline()) {
            target.sendMessage(ChatColor.RED + "That player is no longer online.");
            return true;
        }

        if (request.type == RequestType.TPA) {
            requester.teleport(target.getLocation());
            requester.playSound(requester.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else {
            target.teleport(requester.getLocation());
            target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }

        target.sendMessage(ChatColor.WHITE + "Teleport request accepted.");
        requester.sendMessage(ChatColor.WHITE + target.getName() + " accepted your teleport request.");

        return true;
    }

    private boolean handleCancel(Player target) {
        Request request = pendingRequests.get(target.getUniqueId());

        if (request == null) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return true;
        }

        Player requester = Bukkit.getPlayer(request.requesterId);
        clearRequest(target.getUniqueId());

        target.sendMessage(ChatColor.WHITE + "Teleport request cancelled.");
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.WHITE + target.getName() + " cancelled your teleport request.");
        }

        return true;
    }

    private void clearRequest(UUID targetId) {
        Request existing = pendingRequests.remove(targetId);
        if (existing != null) {
            existing.timeoutTask.cancel();
        }
    }
}
