package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;

import javax.inject.Provider;

public class PasteSignEditInteraction implements SignEditInteraction {
    private final SignTextClipboardManager clipboardManager;
    private final Provider<SignText> signTextProvider;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    public PasteSignEditInteraction(
            SignTextClipboardManager clipboardManager,
            Provider<SignText> signTextProvider,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.clipboardManager = clipboardManager;
        this.signTextProvider = signTextProvider;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public void interact(Player player, Sign sign) {
        SignText clipboard = clipboardManager.getClipboard(player);
        SignText signText = signTextProvider.get();
        signText.setTargetSign(sign);

        for (int i = 0; i < clipboard.getLines().length; i++) {
            signText.setLineLiteral(i, clipboard.getLine(i));
        }
        signText.applySign();
        if (signText.signChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }

    @Override
    public String getName() {
        return "Paste lines in clipboard";
    }
}
