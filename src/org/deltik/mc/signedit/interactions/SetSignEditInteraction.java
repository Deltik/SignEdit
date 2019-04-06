package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;

public class SetSignEditInteraction implements SignEditInteraction {
    private SignText signText;
    private final ChatComms comms;

    public SetSignEditInteraction(SignText signText, ChatComms comms) {
        this.signText = signText;
        this.comms = comms;
    }

    @Override
    public String getName() {
        return "Change sign";
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.applySign();

        comms.compareSignText(signText);
    }
}
