package org.deltik.mc.signedit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.listeners.Interact;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends JavaPlugin {
    public static final String CHAT_PREFIX = "§7[§cSignEdit§7]§r ";
    public static Main instance;
    private Configuration config;
    private Interact listener;
    private SignCommand signCommand;

    @Override
    public void onEnable() {
        Main.instance = this;
        config = new Configuration();
        listener = new Interact(config);
        signCommand = new SignCommand(config, listener);
        for (String alias : new String[] {"sign", "signedit", "editsign", "se"}) {
            this.getCommand(alias).setExecutor(signCommand);
        }
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        Configuration endConfig = new Configuration();
        endConfig.writeFullConfig();
    }
}
