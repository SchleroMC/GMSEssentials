package com.schlero.gmsessentials;

import com.schlero.gmsessentials.commands.BackCommand;
import com.schlero.gmsessentials.commands.FlyCommand;
import com.schlero.gmsessentials.commands.GamemodeCommand;
import com.schlero.gmsessentials.commands.GmseCommand;
import com.schlero.gmsessentials.commands.HomeCommand;
import com.schlero.gmsessentials.commands.InvseeCommand;
import com.schlero.gmsessentials.commands.SizeCommand;
import com.schlero.gmsessentials.commands.TPASystem;
import org.bukkit.plugin.java.JavaPlugin;

public class GmsePlugin extends JavaPlugin {

    public static final String PREFIX = "§6[GMSE] §r";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Register Command Executors
        getCommand("size").setExecutor(new SizeCommand());

        GamemodeCommand gmCmd = new GamemodeCommand();
        getCommand("gms").setExecutor(gmCmd);
        getCommand("gmc").setExecutor(gmCmd);
        getCommand("gma").setExecutor(gmCmd);
        getCommand("gmsp").setExecutor(gmCmd);

        HomeCommand homeCmd = new HomeCommand(this);
        getCommand("sethome").setExecutor(homeCmd);
        getCommand("home").setExecutor(homeCmd);

        InvseeCommand invseeCmd = new InvseeCommand();
        getCommand("invsee").setExecutor(invseeCmd);
        getServer().getPluginManager().registerEvents(invseeCmd, this);

        getCommand("fly").setExecutor(new FlyCommand());

        BackCommand backCmd = new BackCommand(this);
        getCommand("back").setExecutor(backCmd);
        getServer().getPluginManager().registerEvents(backCmd, this);

        getCommand("gmse").setExecutor(new GmseCommand(this));

        TPASystem tpaSystem = new TPASystem(this);
        getCommand("tpa").setExecutor(tpaSystem);
        getCommand("tpahere").setExecutor(tpaSystem);
        getCommand("tpaccept").setExecutor(tpaSystem);
        getCommand("tpacancel").setExecutor(tpaSystem);

        getLogger().info("GMSEssentials has been enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("GMSEssentials has been disabled!");
    }
}
