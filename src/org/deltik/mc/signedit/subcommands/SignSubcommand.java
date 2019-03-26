package org.deltik.mc.signedit.subcommands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.listeners.Interact;

import java.util.Set;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public interface SignSubcommand {
    boolean execute();

    static Block getTargetBlockOfPlayer(Player player) {
        return player.getTargetBlock((Set<Material>) null, 10);
    }

    static boolean autocommit(SignEditCommit commit, Player player, Interact listener, Configuration config) {
        Block block = getTargetBlockOfPlayer(player);
        if (shouldDoClickingMode(block, config)) {
            listener.pendSignEditCommit(player, commit);
            player.sendMessage(CHAT_PREFIX + "§6Now right-click a sign to edit it");
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            commit.validatedCommit(player, sign);
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to edit it!");
            return false;
        }
        return true;
    }

    static boolean shouldDoClickingMode(Block block, Configuration config) {
        if (!config.allowedToEditSignByRightClick())
            return false;
        else if (block == null)
            return true;
        else if (config.allowedToEditSignBySight() && block.getState() instanceof Sign)
            return false;
        return true;
    }

    static boolean reportLineSelectionError(Exception selectedLinesError, Player player) {
        if (!(selectedLinesError instanceof LineSelectionException)) return false;

        player.sendMessage(CHAT_PREFIX + selectedLinesError.getMessage());

        return true;
    }
}