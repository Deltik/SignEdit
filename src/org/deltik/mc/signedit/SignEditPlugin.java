package org.deltik.mc.signedit;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.commands.SignCommandTabCompleter;
import org.deltik.mc.signedit.listeners.Interact;

import javax.inject.Inject;

public class SignEditPlugin extends JavaPlugin {
    public static final String CHAT_PREFIX = "§7[§6SignEdit§7]§r ";

    @Inject
    public Configuration config;

    @Inject
    public Interact listener;

    @Inject
    public SignCommand signCommand;
    @Inject
    public SignCommandTabCompleter signCommandTabCompleter;

    @Override
    public void onEnable() {
        DaggerSignEditPluginComponent.builder().build().injectSignEditPlugin(this);
        for (String alias : new String[]{"sign", "signedit", "editsign", "se"}) {
            PluginCommand pluginCommand = this.getCommand(alias);
            pluginCommand.setExecutor(signCommand);
            pluginCommand.setTabCompleter(signCommandTabCompleter);
        }
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        config.writeFullConfig();
    }
}
