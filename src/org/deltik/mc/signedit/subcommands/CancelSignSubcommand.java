package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;
import org.deltik.mc.signedit.committers.SignEditCommit;

import javax.inject.Inject;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class CancelSignSubcommand implements SignSubcommand {
    private final Interact listener;
    private final Player player;

    @Inject
    public CancelSignSubcommand(Interact listener, Player player) {
        this.listener = listener;
        this.player = player;
    }

    @Override
    public boolean execute() {
        SignEditCommit commit = listener.popSignEditCommit(player);
        if (commit != null) {
            player.sendMessage(CHAT_PREFIX + "§6Cancelled pending right-click action");
        } else {
            player.sendMessage(CHAT_PREFIX + "§cNo right-click action to cancel!");
        }
        return true;
    }
}
