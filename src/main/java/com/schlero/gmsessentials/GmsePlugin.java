package com.schlero.gmsessentials;

import com.schlero.gmsessentials.commands.BackCommand;
import com.schlero.gmsessentials.commands.FlyCommand;
import com.schlero.gmsessentials.commands.GamemodeCommand;
import com.schlero.gmsessentials.commands.GmseCommand;
import com.schlero.gmsessentials.commands.HomeCommand;
import com.schlero.gmsessentials.commands.InvseeCommand;
import com.schlero.gmsessentials.commands.SizeCommand;
import com.schlero.gmsessentials.commands.TPASystem;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

public class GmsePlugin extends JavaPlugin {

    private HomeManager homeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        homeManager = new HomeManager(this);

        // Commands are registered with Paper's Brigadier API instead of plugin.yml.
        registerCommand("size", "Change a player's scale attribute.", new SizeCommand());

        registerCommand("gms", "Shortcut for Survival mode.", new GamemodeCommand(GameMode.SURVIVAL, "Survival"));
        registerCommand("gmc", "Shortcut for Creative mode.", new GamemodeCommand(GameMode.CREATIVE, "Creative"));
        registerCommand("gma", "Shortcut for Adventure mode.", new GamemodeCommand(GameMode.ADVENTURE, "Adventure"));
        registerCommand("gmsp", "Shortcut for Spectator mode.", new GamemodeCommand(GameMode.SPECTATOR, "Spectator"));

        HomeCommand homeCommand = new HomeCommand(this);
        registerCommand("sethome", "Sets your home location.", (source, args) -> homeCommand.setHome(source));
        registerCommand("home", "Teleports you to your home location.", (source, args) -> homeCommand.teleportHome(source));

        InvseeCommand invseeCommand = new InvseeCommand(this);
        registerCommand("invsee", "Inspect and manage another player's inventory.", invseeCommand);
        getServer().getPluginManager().registerEvents(invseeCommand, this);

        registerCommand("fly", "Toggle fly ability for yourself or another player.", new FlyCommand());

        BackCommand backCommand = new BackCommand(this);
        registerCommand("back", "Teleports you to your last death location.", backCommand);
        getServer().getPluginManager().registerEvents(backCommand, this);

        registerCommand("gmse", "GMSEssentials management command.", new GmseCommand(this));

        TPASystem tpaSystem = new TPASystem(this);
        registerCommand("tpa", "Request to teleport to another player.", tpaSystem.command(TPASystem.Action.TPA));
        registerCommand("tpahere", "Request that another player teleports to you.", tpaSystem.command(TPASystem.Action.TPAHERE));
        registerCommand("tpaccept", "Accept a pending teleport request.", tpaSystem.command(TPASystem.Action.ACCEPT));
        registerCommand("tpacancel", "Cancel a pending teleport request.", tpaSystem.command(TPASystem.Action.CANCEL));

        getLogger().info("GMSEssentials has been enabled!");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.save();
        }
        getLogger().info("GMSEssentials has been disabled!");
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }
}
