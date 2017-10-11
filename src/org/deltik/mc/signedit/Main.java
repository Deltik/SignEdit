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
    public static final String MINECRAFT_SERVER_VERSION = getMinecraftServerVersion();

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
        // XXX
        getLogger().warning(MINECRAFT_SERVER_VERSION);
    }

    @Override
    public void onDisable() {
        Configuration endConfig = new Configuration();
        endConfig.writeFullConfig();
    }

    private static String getMinecraftServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public Class<?> getMinecraftServerClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + MINECRAFT_SERVER_VERSION + "." + className);
        } catch (ClassNotFoundException | NullPointerException e) {
            PrintWriter w = new PrintWriter(new StringWriter());
            e.printStackTrace(w);
            getLogger().severe(w.toString());
            return null;
        }
    }
}
