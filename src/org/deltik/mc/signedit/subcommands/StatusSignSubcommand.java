package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.*;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class StatusSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final Player player;
    private final SignEditListener listener;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;

    @Inject
    public StatusSignSubcommand(
            Configuration config,
            Player player,
            SignEditListener listener,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager
    ) {
        this.config = config;
        this.player = player;
        this.listener = listener;
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        reportHistory();
        reportPendingAction();
        reportClipboard();
        return null;
    }

    private void reportPendingAction() {
        if (!listener.isInteractionPending(player)) {
            player.sendMessage(CHAT_PREFIX + "§6§lPending Action:§r §7None");
        } else {
            SignEditInteraction interaction = listener.getPendingInteraction(player);
            player.sendMessage(CHAT_PREFIX + "§6§lPending Action:§r " + interaction.getName());
            player.sendMessage(CHAT_PREFIX + "  §oRight-click a sign to apply the action!");
        }
    }

    private void reportHistory() {
        SignTextHistory history = historyManager.getHistory(player);
        int undosRemaining = 0;
        int redosRemaining = 0;

        if (history != null) {
            undosRemaining = history.undosRemaining();
            redosRemaining = history.redosRemaining();
        }

        player.sendMessage(CHAT_PREFIX + "§6§lHistory:§r §6have §e" +
                undosRemaining +
                "§6 undos, §e" +
                redosRemaining +
                "§6 redos");
    }

    private void reportClipboard() {
        SignText clipboard = clipboardManager.getClipboard(player);
        if (clipboard == null) {
            player.sendMessage(CHAT_PREFIX + "§6§lClipboard Contents:§r §7None");
        } else {
            player.sendMessage(CHAT_PREFIX + "§6§lClipboard Contents:");
            for (int i = 0; i < 4; i++) {
                String line = clipboard.getLine(i);
                int relativeLineNumber = i + config.getMinLine();
                if (line == null) {
                    player.sendMessage(CHAT_PREFIX + "§6§l  Line " + relativeLineNumber + "§r §7is undefined.");
                } else {
                    player.sendMessage(CHAT_PREFIX + "§6§l  Line " + relativeLineNumber + ":§r " + line);
                }
            }
        }
    }
}
