package org.deltik.mc.signedit;

import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.listeners.Interact;

import javax.inject.Inject;

public class SignEditPlugin extends JavaPlugin {
    public static final String CHAT_PREFIX = "§7[§6SignEdit§7]§r ";

    @Deprecated
    public static SignEditPlugin instance;

    @Inject
    public Configuration config;

    @Inject
    public Interact listener;

    @Inject
    public SignCommand signCommand;

    @Override
    public void onEnable() {
        SignEditPlugin.instance = this;
        DaggerSignEditPluginComponent.create().injectSignEditPlugin(this);
        for (String alias : new String[]{"sign", "signedit", "editsign", "se"}) {
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
