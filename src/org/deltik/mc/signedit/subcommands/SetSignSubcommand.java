package org.deltik.mc.signedit.subcommands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class SetSignSubcommand extends SignSubcommand {
    @Override
    public boolean execute() {
        int minLine = config.getMinLine();
        int maxLine = config.getMaxLine();
        if (argStruct.lineRelative > maxLine || argStruct.lineRelative < minLine) {
            player.sendMessage(CHAT_PREFIX + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
            return false;
        }
        int line = argStruct.lineRelative - minLine;

        String txt;
        if (argStruct.subcommand.equals("clear")) {
            txt = "";
        } else {
            txt = arrayToSignText(argStruct.remainder);
        }

        Block block = getTargetBlockOfPlayer(player);

        if (shouldDoClickingMode(block)) {
            return pendSignEdit(player, line, txt);
        } else if (block.getState() instanceof Sign) {
            Sign s = (Sign) block.getState();
            playerEditSignLine(player, s, line, txt, config);
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to edit it!");
            return false;
        }

        return true;
    }

    private boolean pendSignEdit(Player player, int line, String text) {
        HashMap<Integer, String> pendingSignEdit = new HashMap<>();
        pendingSignEdit.put(line, text);
        listener.pendingSignEdits.put(player, pendingSignEdit);
        player.sendMessage(CHAT_PREFIX + "§cNow right-click a sign to set the line");
        return true;
    }

    private String arrayToSignText(List<String> textArray) {
        return String.join(" ", textArray).replace('&', '§');
    }

    public static void playerEditSignLine(Player p, Sign s, int line, String text, Configuration config) {
        String before = s.getLine(line);
        s.setLine(line, text);
        s.update();
        int lineRelative = line + config.getMinLine();
        if (text.isEmpty())
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c blanked");
        else if (text.equals(before))
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c unchanged");
        else {
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c changed");
            p.sendMessage(CHAT_PREFIX + "§c§lBefore: §r" + before);
            p.sendMessage(CHAT_PREFIX + "§c §l After: §r" + text);
        }
    }
}
