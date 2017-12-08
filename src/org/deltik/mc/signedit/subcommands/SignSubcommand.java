package org.deltik.mc.signedit.subcommands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.listeners.Interact;

import java.util.Set;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public abstract class SignSubcommand {
    Configuration config;
    Interact listener;
    ArgStruct argStruct;
    Player player;

    public abstract boolean execute();

    public void setDependencies(Configuration c, Interact l, ArgStruct args, Player p) {
        config = c;
        listener = l;
        argStruct = args;
        player = p;
    }

    Block getTargetBlockOfPlayer(Player player) {
        return player.getTargetBlock((Set<Material>) null, 10);
    }

    boolean autocommit(SignEditCommit commit) {
        Block block = getTargetBlockOfPlayer(player);
        if (shouldDoClickingMode(block)) {
            listener.pendSignEditCommit(player, commit);
            player.sendMessage(CHAT_PREFIX + "§cNow right-click a sign to edit it");
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            commit.validatedCommit(player, sign);
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to edit it!");
            return false;
        }
        return true;
    }

    boolean shouldDoClickingMode(Block block) {
        if (!config.allowedToEditSignByRightClick())
            return false;
        else if (block == null)
            return true;
        else if (config.allowedToEditSignBySight() && block.getState() instanceof Sign)
            return false;
        return true;
    }
}