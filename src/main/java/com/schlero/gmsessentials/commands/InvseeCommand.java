package com.schlero.gmsessentials.commands;

import com.schlero.gmsessentials.GmsePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InvseeCommand implements CommandExecutor, Listener {

    // Track active Invsee sessions: Admin UUID -> Target UUID
    private final Map<UUID, UUID> activeSessions = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use /invsee.");
            return true;
        }

        Player admin = (Player) sender;

        if (args.length == 0) {
            admin.sendMessage(ChatColor.RED + "Usage: /invsee <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            admin.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (target.equals(admin)) {
            admin.sendMessage(ChatColor.RED + "You cannot invsee yourself.");
            return true;
        }

        openInvseeMenu(admin, target);
        return true;
    }

    public void openInvseeMenu(Player admin, Player target) {
        String title = target.getName() + "'s Inventory";
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
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }

        // Slots 36-44: Spacer row
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, filler);
        }

        // Bottom row (Slots 45-53) layout matching the screenshot:
        // 45: Filler, 46: Filler, 47: Helmet, 48: Chestplate, 49: Filler, 50: Leggings, 51: Boots, 52: Offhand/Filler, 53: Filler
        gui.setItem(45, filler);
        gui.setItem(46, filler);
        gui.setItem(47, targetInv.getHelmet());
        gui.setItem(48, targetInv.getChestplate());
        gui.setItem(49, filler);
        gui.setItem(50, targetInv.getLeggings());
        gui.setItem(51, targetInv.getBoots());
        gui.setItem(52, targetInv.getItemInOffHand() != null && targetInv.getItemInOffHand().getType() != Material.AIR ? targetInv.getItemInOffHand() : filler);
        gui.setItem(53, filler);

        activeSessions.put(admin.getUniqueId(), target.getUniqueId());
        admin.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player admin = (Player) event.getWhoClicked();

        if (!activeSessions.containsKey(admin.getUniqueId())) return;

        UUID targetUUID = activeSessions.get(admin.getUniqueId());
        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            admin.closeInventory();
            admin.sendMessage(ChatColor.RED + "Target player logged off.");
            return;
        }

        int slot = event.getRawSlot();

        // Prevent clicking filler items
        if (event.getCurrentItem() != null &&
            event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE &&
            event.getCurrentItem().hasItemMeta() &&
            " ".equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
            event.setCancelled(true);
            return;
        }

        // Sync changes back to target inventory shortly after click
        Bukkit.getScheduler().runTask(GmsePlugin.getPlugin(GmsePlugin.class), () -> {
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
            if (offhandCandidate != null && offhandCandidate.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                targetInv.setItemInOffHand(offhandCandidate);
            } else if (offhandCandidate == null) {
                targetInv.setItemInOffHand(null);
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            activeSessions.remove(event.getPlayer().getUniqueId());
        }
    }
}
