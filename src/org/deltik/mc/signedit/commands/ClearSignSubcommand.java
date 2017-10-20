package org.deltik.mc.signedit.commands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;
import org.deltik.mc.signedit.subcommands.SetSignSubcommand;

public class ClearSignSubcommand extends SetSignSubcommand {
    public ClearSignSubcommand(Configuration c, Interact l, ArgStruct args, Player p) {
        super(c, l, args, p);
    }
}