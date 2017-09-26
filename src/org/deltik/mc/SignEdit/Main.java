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

    public void playerEditSignLine(Player p, Sign s, int line, String text) {
        String before = s.getLine(line);
        s.setLine(line, text);
        s.update();
        int lineRelative = line + config.getMinLine();
        if (text.isEmpty())
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c blanked");
        else if (text.equals(before))
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c unchanged");
        else {
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c changed");
            p.sendMessage(Main.prefix + "§c§lBefore: §r" + before);
            p.sendMessage(Main.prefix + "§c §l After: §r" + text);
        }
    }
}
