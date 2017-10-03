package org.deltik.mc.SignEdit;

import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.SignEdit.Commands.SignCommand;
import org.deltik.mc.SignEdit.EventHandler.Interact;
import org.deltik.mc.SignEdit.Configuration;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    public static String prefix = "§7[§cSignEdit§7]§r ";
    public static Main instance;
    public static Configuration config;

    public void onEnable() {
        Main.instance = this;
        config = new Configuration();
        for (String alias : new String[] {"sign", "signedit", "editsign", "se"}) {
            this.getCommand(alias).setExecutor(new SignCommand(config));
        }
        getServer().getPluginManager().registerEvents(new Interact(), this);
    }

    public void onDisable() {
        Configuration endConfig = new Configuration();
        endConfig.writeFullConfig();
    }
}
