package org.deltik.mc.signedit.committers;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

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
            player.sendMessage(CHAT_PREFIX + "§6Line §e" + lineRelative + "§6 blanked");
        else if (text.equals(before))
            player.sendMessage(CHAT_PREFIX + "§6Line §e" + lineRelative + "§6 unchanged");
        else {
            player.sendMessage(CHAT_PREFIX + "§6Line §e" + lineRelative + "§6 changed");
            player.sendMessage(CHAT_PREFIX + "§6§lBefore: §r" + before);
            player.sendMessage(CHAT_PREFIX + "§6 §l After: §r" + text);
        }
    }
}
