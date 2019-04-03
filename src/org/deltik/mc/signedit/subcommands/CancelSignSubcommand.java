package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class CancelSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private final Player player;
    private final ChatComms comms;

    @Inject
    public CancelSignSubcommand(SignEditListener listener, Player player, ChatComms comms) {
        this.listener = listener;
        this.player = player;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        SignEditInteraction interaction = listener.removePendingInteraction(player);
        if (interaction != null) {
            comms.tellPlayer(comms.primary() + "Cancelled pending right-click action");
        } else {
            comms.tellPlayer(comms.error() + "No right-click action to cancel!");
        }
        return null;
    }
}
