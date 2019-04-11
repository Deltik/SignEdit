package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;

public class SetSignEditInteraction implements SignEditInteraction {
    private SignText signText;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    public SetSignEditInteraction(SignText signText, ChatComms comms, SignTextHistoryManager historyManager) {
        this.signText = signText;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "change_sign_text";
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.applySign();
        if (signText.signChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }
}
