package org.deltik.mc.signedit;

import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class SignTextClipboardManager {
    private Map<Player, SignText> playerSignTextMap;

    @Inject
    public SignTextClipboardManager(Map<Player, SignText> playerSignTextMap) {
        this.playerSignTextMap = playerSignTextMap;
    }

    public void forgetPlayer(Player player) {
        playerSignTextMap.remove(player);
    }

    public SignText getClipboard(Player player) {
        return playerSignTextMap.get(player);
    }

    public void setClipboard(Player player, SignText clipboard) {
        playerSignTextMap.put(player, clipboard);
    }
}
