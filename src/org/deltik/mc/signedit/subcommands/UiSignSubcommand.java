package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private MinecraftReflector reflector;
    private final ChatComms comms;

    @Inject
    public UiSignSubcommand(
            SignEditListener listener,
            MinecraftReflector reflector,
            ChatComms comms
    ) {
        this.listener = listener;
        this.reflector = reflector;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        return new UiSignEditInteraction(reflector, listener, comms);
    }
}