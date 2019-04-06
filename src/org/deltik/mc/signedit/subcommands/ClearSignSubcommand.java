package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;

import javax.inject.Inject;

public class ClearSignSubcommand extends SetSignSubcommand {
    @Inject
    public ClearSignSubcommand(ArgParser args, SignText t, ChatComms comms) {
        super(args, t, comms);
    }
}