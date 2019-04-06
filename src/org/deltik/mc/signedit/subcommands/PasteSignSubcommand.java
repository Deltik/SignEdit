package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.NullClipboardException;
import org.deltik.mc.signedit.interactions.PasteSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;

public class PasteSignSubcommand implements SignSubcommand {
    private final Player player;
    private final SignTextClipboardManager clipboardManager;
    private final Provider<SignText> signTextProvider;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public PasteSignSubcommand(
            Player player,
            SignTextClipboardManager clipboardManager,
            Provider<SignText> signTextProvider,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.player = player;
        this.clipboardManager = clipboardManager;
        this.signTextProvider = signTextProvider;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        if (clipboardManager.getClipboard(player) == null) {
            throw new NullClipboardException();
        }

        return new PasteSignEditInteraction(clipboardManager, signTextProvider, historyManager, comms);
    }
}
