package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import com.schlero.gmsessentials.Messages;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvseeCommand implements BasicCommand, Listener {

    private static final Material FILLER_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;
    private static final Component FILLER_NAME = Component.text(" ");

    private final GmsePlugin plugin;
    private final Messages messages;
    // Track active Invsee sessions: Admin UUID -> Target UUID
    private final Map<UUID, UUID> activeSessions = new HashMap<>();

    public InvseeCommand(GmsePlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (!(sender instanceof Player admin)) {
            sender.sendMessage(messages.get("invsee.players-only"));
            return;
        }

        if (args.length == 0) {
            admin.sendMessage(messages.get("invsee.usage"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            admin.sendMessage(messages.get("generic.player-not-found"));
            return;
        }

        if (target.equals(admin)) {
            admin.sendMessage(messages.get("invsee.cannot-invsee-self"));
            return;
        }

        openInvseeMenu(admin, target);
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
        return "gmse.admin";
    }

    public void openInvseeMenu(Player admin, Player target) {
        Component title = Component.text(target.getName() + "'s Inventory");
        Inventory gui = Bukkit.createInventory(null, 54, title);

        PlayerInventory targetInv = target.getInventory();

        // Slots 0-8: Hotbar items (0-8 in player inventory)
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, targetInv.getItem(i));
        }

        // Slots 9-35: Main storage items (9-35 in player inventory)
        for (int i = 9; i < 36; i++) {
            gui.setItem(i, targetInv.getItem(i));
        }

        // Filler Glass for slot formatting
        ItemStack filler = new ItemStack(FILLER_MATERIAL);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(FILLER_NAME);
            filler.setItemMeta(meta);
        }

        // Slots 36-44: Spacer row
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, filler);
        }

        // Bottom row (Slots 45-53):
        // 45: Filler, 46: Filler, 47: Helmet, 48: Chestplate, 49: Filler, 50: Leggings, 51: Boots, 52: Offhand/Filler, 53: Filler
        gui.setItem(45, filler);
        gui.setItem(46, filler);
        gui.setItem(47, targetInv.getHelmet());
        gui.setItem(48, targetInv.getChestplate());
        gui.setItem(49, filler);
        gui.setItem(50, targetInv.getLeggings());
        gui.setItem(51, targetInv.getBoots());

        ItemStack offhand = targetInv.getItemInOffHand();
        gui.setItem(52, offhand.getType() != Material.AIR ? offhand : filler);
        gui.setItem(53, filler);

        activeSessions.put(admin.getUniqueId(), target.getUniqueId());
        admin.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) return;
        if (!activeSessions.containsKey(admin.getUniqueId())) return;

        UUID targetUUID = activeSessions.get(admin.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            admin.closeInventory();
            admin.sendMessage(messages.get("invsee.target-logged-off"));
            return;
        }

        // Prevent clicking filler items
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null
                && currentItem.getType() == FILLER_MATERIAL
                && currentItem.hasItemMeta()
                && FILLER_NAME.equals(currentItem.getItemMeta().displayName())) {
            event.setCancelled(true);
            return;
        }

        // Sync changes back to target inventory shortly after click
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerInventory targetInv = target.getInventory();
            Inventory top = event.getInventory();

            // Sync main inventory (Slots 0-35)
            for (int i = 0; i < 36; i++) {
                targetInv.setItem(i, top.getItem(i));
            }

            // Sync armor and offhand
            targetInv.setHelmet(top.getItem(47));
            targetInv.setChestplate(top.getItem(48));
            targetInv.setLeggings(top.getItem(50));
            targetInv.setBoots(top.getItem(51));

            ItemStack offhandCandidate = top.getItem(52);
            if (offhandCandidate != null && offhandCandidate.getType() != FILLER_MATERIAL) {
                targetInv.setItemInOffHand(offhandCandidate);
            } else if (offhandCandidate == null) {
                targetInv.setItemInOffHand(null);
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            activeSessions.remove(player.getUniqueId());
        }
    }
}
