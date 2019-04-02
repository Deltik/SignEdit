package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.SignEditPlugin;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class VersionSignSubcommand implements SignSubcommand {
    private Player player;
    private SignEditPlugin plugin;

    @Inject
    public VersionSignSubcommand(Player player, SignEditPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public SignEditInteraction execute() {
        String version = plugin.getDescription().getVersion();
        player.sendMessage(CHAT_PREFIX + "§6Version§r " + version);
        return null;
    }
}
