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
    boolean execute();

    static Block getTargetBlockOfPlayer(Player player) {
        return player.getTargetBlock(null, 10);
    }

    static boolean autointeract(SignEditInteraction interaction, Player player, SignEditListener listener, Configuration config) {
        Block block = getTargetBlockOfPlayer(player);
        if (shouldDoClickingMode(block, config)) {
            listener.setPendingInteraction(player, interaction);
            player.sendMessage(CHAT_PREFIX + "§6Now right-click a sign to edit it");
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            interaction.validatedInteract(player, sign);
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
        else return !config.allowedToEditSignBySight() || !(block.getState() instanceof Sign);
    }

    static boolean reportLineSelectionError(Exception selectedLinesError, Player player) {
        if (!(selectedLinesError instanceof LineSelectionException)) return false;

        player.sendMessage(CHAT_PREFIX + "§c" + selectedLinesError.getMessage());

        return true;
    }
}