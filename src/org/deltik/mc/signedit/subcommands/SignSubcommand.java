package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.interactions.SignEditInteraction;

public interface SignSubcommand {
    SignEditInteraction execute();
}