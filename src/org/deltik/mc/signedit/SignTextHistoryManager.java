package org.deltik.mc.signedit;

import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignTextHistoryManager {
    private Map<Player, SignTextHistory> playerHistoryMap = new HashMap<>();
    private Provider<SignTextHistory> historyProvider;

    @Inject
    public SignTextHistoryManager(Provider<SignTextHistory> historyProvider) {
        this.historyProvider = historyProvider;
    }

    public void forgetPlayer(Player player) {
        playerHistoryMap.remove(player);
    }

    public SignTextHistory getHistory(Player player) {
        SignTextHistory history = playerHistoryMap.get(player);
        if (history == null) {
            history = historyProvider.get();
            playerHistoryMap.put(player, history);
        }
        return history;
    }
}
