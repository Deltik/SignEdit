package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.interactions.CopySignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class CopySignSubcommand implements SignSubcommand {
    private final ArgParser argParser;
    private final SignText signText;
    private final ChatComms comms;
    private final SignTextClipboardManager clipboardManager;

    @Inject
    public CopySignSubcommand(
            ArgParser argParser,
            SignText signText,
            ChatComms comms,
            SignTextClipboardManager clipboardManager
    ) {
        this.argParser = argParser;
        this.signText = signText;
        this.comms = comms;
        this.clipboardManager = clipboardManager;
    }

    @Override
    public SignEditInteraction execute() {
        return new CopySignEditInteraction(argParser, signText, clipboardManager, comms);
    }
}
