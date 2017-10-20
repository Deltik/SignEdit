package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;

public abstract class SignSubcommand {
    Configuration config;
    Interact listener;
    ArgStruct argStruct;
    Player player;

    public SignSubcommand(Configuration c, Interact l, ArgStruct args, Player p) {
        config = c;
        listener = l;
        argStruct = args;
        player = p;
    }
    public abstract boolean execute();
}