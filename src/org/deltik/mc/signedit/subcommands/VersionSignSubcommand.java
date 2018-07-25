package org.deltik.mc.signedit.subcommands;

import org.bukkit.plugin.PluginDescriptionFile;
import org.deltik.mc.signedit.Main;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class VersionSignSubcommand extends SignSubcommand {
    @Override
    public boolean execute() {
        String version = Main.instance.getDescription().getVersion();
        player.sendMessage(CHAT_PREFIX + "§6Version§r " + version);
        return true;
    }
}
