package org.deltik.mc.signedit.committers;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class LineSignEditCommit implements SignEditCommit {
    private int lineNumber;
    private int lineOffset;
    private String text;

    public LineSignEditCommit(int lineNumber, int lineOffset, String text) {
        this.lineNumber = lineNumber;
        this.lineOffset = lineOffset;
        this.text = text;
    }

    @Override
    public void commit(Player player, Sign sign) {
        String before = sign.getLine(lineNumber);
        sign.setLine(lineNumber, text);
        sign.update();
        int lineRelative = lineNumber + lineOffset;
        if (text.isEmpty())
            player.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c blanked");
        else if (text.equals(before))
            player.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c unchanged");
        else {
            player.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c changed");
            player.sendMessage(CHAT_PREFIX + "§c§lBefore: §r" + before);
            player.sendMessage(CHAT_PREFIX + "§c §l After: §r" + text);
        }
    }
}
