package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.SignText;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class SetSignEditInteraction implements SignEditInteraction {
    private SignText signText;
    private int lineOffset;

    public SetSignEditInteraction(SignText signText, int lineOffset) {
        this.signText = signText;
        this.lineOffset = lineOffset;
    }

    @Override
    public String getName() {
        return "Change sign";
    }

    @Override
    public void interact(Player player, Sign sign) {
        SignText beforeSignText = new SignText();
        beforeSignText.setTargetSign(sign);
        beforeSignText.importSign();

        signText.setTargetSign(sign);
        signText.applySign();
        signText.importSign();

        if (!signText.equals(beforeSignText)) {
            player.sendMessage(CHAT_PREFIX + "§6§lBefore:");
            printSignLines(player, beforeSignText);
            player.sendMessage(CHAT_PREFIX + "§6§lAfter:");
            printSignLines(player, signText);
        } else {
            player.sendMessage(CHAT_PREFIX + "§6Sign did not change");
        }
    }

    private void printSignLines(Player player, SignText signText) {
        for (int i = 0; i < 4; i++) {
            int relativeLineNumber = lineOffset + i;
            String line = signText.getLine(i);
            if (line == null) {
                player.sendMessage(CHAT_PREFIX + "§6§l  Line " + relativeLineNumber + "§r §7is undefined.");
            } else {
                player.sendMessage(CHAT_PREFIX + "§6§l  Line " + relativeLineNumber + ":§r " + line);
            }
        }
    }
}
