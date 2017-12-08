package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.committers.UiSignEditCommit;

public class UiSignSubcommand extends SignSubcommand {
    private MinecraftReflector reflector;

    // Create a MinecraftReflector (used in production)
    public UiSignSubcommand() {
        this(new MinecraftReflector());
    }

    // Provide a MinecraftReflector (useful in tests)
    public UiSignSubcommand(MinecraftReflector r) {
        reflector = r;
    }

    @Override
    public boolean execute() {
        SignEditCommit commit = new UiSignEditCommit(reflector, listener);
        return autocommit(commit);
    }
}