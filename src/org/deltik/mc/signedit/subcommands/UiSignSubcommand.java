package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private final SignText signText;
    private final MinecraftReflector reflector;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public UiSignSubcommand(
            SignEditListener listener,
            SignText signText,
            MinecraftReflector reflector,
            ChatComms comms,
            SignTextHistoryManager historyManager
    ) {
        this.listener = listener;
        this.signText = signText;
        this.reflector = reflector;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        return new UiSignEditInteraction(reflector, listener, comms, signText, historyManager);
    }
}