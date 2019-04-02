package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final SignEditListener listener;
    private MinecraftReflector reflector;

    @Inject
    public UiSignSubcommand(
            Configuration config,
            SignEditListener listener,
            MinecraftReflector reflector
    ) {
        this.config = config;
        this.listener = listener;
        this.reflector = reflector;
    }

    @Override
    public SignEditInteraction execute() {
        return new UiSignEditInteraction(reflector, listener, config.getLineStartsAt());
    }
}