package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private final SignText signText;
    private MinecraftReflector reflector;
    private final ChatComms comms;

    @Inject
    public UiSignSubcommand(
            SignEditListener listener,
            SignText signText,
            MinecraftReflector reflector,
            ChatComms comms
    ) {
        this.listener = listener;
        this.signText = signText;
        this.reflector = reflector;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        return new UiSignEditInteraction(reflector, listener, comms, signText);
    }
}