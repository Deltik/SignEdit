package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.*;
import org.deltik.mc.signedit.interactions.CopySignEditInteraction;
import org.deltik.mc.signedit.interactions.PasteSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;

public class PasteSignSubcommand implements SignSubcommand {
    private final SignTextClipboardManager clipboardManager;
    private final Provider<SignText> signTextProvider;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public PasteSignSubcommand(
            SignTextClipboardManager clipboardManager,
            Provider<SignText> signTextProvider,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.clipboardManager = clipboardManager;
        this.signTextProvider = signTextProvider;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        return new PasteSignEditInteraction(clipboardManager, signTextProvider, historyManager, comms);
    }
}
