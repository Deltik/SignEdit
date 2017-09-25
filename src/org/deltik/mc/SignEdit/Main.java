package org.deltik.mc.SignEdit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.SignEdit.Commands.SignCommand;
import org.deltik.mc.SignEdit.EventHandler.Interact;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {
    public static String prefix = "§7[§cSignEdit§7] ";
    public static Main instance;

    public void onEnable() {
        Main.instance = this;
        for (String alias : new String[] {"sign", "signedit", "editsign", "se"}) {
            this.getCommand(alias).setExecutor(new SignCommand());
        }
        getServer().getPluginManager().registerEvents(new Interact(), this);
        createFile();
    }

    public void onDisable() {
    }

    private void createFile() {

        File file = new File("plugins//" + this.getName() + "//", "config.yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            c.set("clicking", false);

            try {
                c.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean click() {
        File file = new File("plugins//" + this.getName() + "//", "config.yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);

        return c.getBoolean("clicking");
    }
}
