package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.*;
import org.deltik.mc.signedit.interactions.CutSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;

public class CutSignSubcommand implements SignSubcommand {
    private final ArgParser argParser;
    private final Provider<SignText> signTextProvider;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public CutSignSubcommand(
            ArgParser argParser,
            Provider<SignText> signTextProvider,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.argParser = argParser;
        this.signTextProvider = signTextProvider;
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        return new CutSignEditInteraction(argParser, signTextProvider, clipboardManager, historyManager, comms);
    }
}
