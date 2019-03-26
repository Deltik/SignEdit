package org.deltik.mc.signedit.listeners;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignEditListener implements Listener {

    private SignTextClipboardManager clipboardManager;
    private SignTextHistoryManager historyManager;

    private Map<Player, SignEditInteraction> pendingInteractions = new HashMap<>();
    private Map<Player, SignEditInteraction> inProgressInteractions = new HashMap<>();

    @Inject
    public SignEditListener(SignTextClipboardManager clipboardManager, SignTextHistoryManager historyManager) {
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();

        if (isInteractionPending(player)) {
            SignEditInteraction interaction = popSignEditInteraction(player);
            interaction.validatedInteract(player, sign);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        clipboardManager.forgetPlayer(player);
        historyManager.forgetPlayer(player);

        if (isInProgress(player)) {
            popInProgressInteraction(player).cleanup();
        }
        popSignEditInteraction(player);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (isInProgress(player)) {
            popInProgressInteraction(player);
            // Apply colors
            String[] lines = event.getLines();
            for (int i = 0; i < lines.length; i++) {
                event.setLine(i, lines[i].replace('&', '§'));
            }
        }
    }

    public void pendSignEditInteraction(Player player, SignEditInteraction interaction) {
        pendingInteractions.put(player, interaction);
    }

    public void registerInProgressInteraction(Player player, SignEditInteraction interaction) {
        inProgressInteractions.put(player, interaction);
    }

    public boolean isInteractionPending(Player player) {
        return pendingInteractions.containsKey(player);
    }

    public boolean isInProgress(Player player) {
        return inProgressInteractions.containsKey(player);
    }

    public SignEditInteraction popSignEditInteraction(Player player) {
        return pendingInteractions.remove(player);
    }

    public SignEditInteraction popInProgressInteraction(Player player) {
        return inProgressInteractions.remove(player);
    }
}
