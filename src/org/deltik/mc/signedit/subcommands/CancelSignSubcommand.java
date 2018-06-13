package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.committers.SignEditCommit;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class CancelSignSubcommand extends SignSubcommand {
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
