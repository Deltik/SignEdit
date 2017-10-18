package org.deltik.mc.signedit.subcommands;

import java.util.List;

public interface SignSubcommand {
    void execute(List<String> args);
}