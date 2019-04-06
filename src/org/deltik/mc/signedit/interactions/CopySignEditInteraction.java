package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;

import java.util.Arrays;

import static org.deltik.mc.signedit.ArgParser.ALL_LINES_SELECTED;
import static org.deltik.mc.signedit.ArgParser.NO_LINES_SELECTED;

public class CopySignEditInteraction implements SignEditInteraction {
    private final ArgParser argParser;
    private final SignText signText;
    private final SignTextClipboardManager clipboardManager;
    private final ChatComms comms;

    public CopySignEditInteraction(
            ArgParser argParser,
            SignText signText,
            SignTextClipboardManager clipboardManager,
            ChatComms comms) {
        this.argParser = argParser;
        this.signText = signText;
        this.clipboardManager = clipboardManager;
        this.comms = comms;
    }

    @Override
    public void interact(Player player, Sign sign) {
        int[] selectedLines = argParser.getSelectedLines();
        if (Arrays.equals(selectedLines, NO_LINES_SELECTED)) {
            selectedLines = ALL_LINES_SELECTED;
        }
        for (int selectedLine : selectedLines) {
            signText.setLineLiteral(selectedLine, sign.getLine(selectedLine));
        }

        clipboardManager.setClipboard(player, signText);

        comms.tellPlayer(comms.primary() + comms.strong() + "Lines copied:");
        comms.dumpLines(signText.getLines());
    }

    @Override
    public String getName() {
        return "Copy sign text";
    }
}
