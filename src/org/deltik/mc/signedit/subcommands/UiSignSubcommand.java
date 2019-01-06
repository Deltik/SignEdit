package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.committers.UiSignEditCommit;
import org.deltik.mc.signedit.listeners.Interact;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final Interact listener;
    private final Player player;
    private MinecraftReflector reflector;

    @Inject
    public UiSignSubcommand(Configuration config, Interact listener, Player player, MinecraftReflector reflector) {
        this.config = config;
        this.listener = listener;
        this.player = player;
        this.reflector = reflector;
    }

    @Override
    public boolean execute() {
        SignEditCommit commit = new UiSignEditCommit(reflector, listener);
        SignSubcommand.autocommit(commit, player, listener, config);
        return true;
    }
}