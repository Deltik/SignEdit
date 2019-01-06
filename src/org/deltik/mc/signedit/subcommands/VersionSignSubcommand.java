package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;
import org.deltik.mc.signedit.SignEditPlugin;

import javax.inject.Inject;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class VersionSignSubcommand implements SignSubcommand {
    private Player player;

    @Inject
    public VersionSignSubcommand(Player player) {
        this.player = player;
    }

    @Override
    public boolean execute() {
        String version = SignEditPlugin.instance.getDescription().getVersion();
        player.sendMessage(CHAT_PREFIX + "§6Version§r " + version);
        return true;
    }
}
