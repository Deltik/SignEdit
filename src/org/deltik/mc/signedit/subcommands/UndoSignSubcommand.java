package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistory;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class UndoSignSubcommand implements SignSubcommand {
    private final Player player;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public UndoSignSubcommand(Player player, ChatComms comms, SignTextHistoryManager historyManager) {
        this.player = player;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        SignTextHistory history = historyManager.getHistory(player);
        SignText undoneSignText = history.undo();
        comms.compareSignText(undoneSignText);
        return null;
    }
}
