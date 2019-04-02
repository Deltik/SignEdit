package org.deltik.mc.signedit.subcommands;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public interface SignSubcommand {
    SignEditInteraction execute();
}