/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.*;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteractionManager;

import javax.inject.Inject;

public class StatusSignSubcommand implements SignSubcommand {
    private final Player player;
    private final ChatComms comms;
    private final SignEditInteractionManager interactionManager;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;

    @Inject
    public StatusSignSubcommand(
            Player player,
            ChatComms comms,
            SignEditInteractionManager interactionManager,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager
    ) {
        this.player = player;
        this.comms = comms;
        this.interactionManager = interactionManager;
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
        if (!interactionManager.isInteractionPending(player)) {
            comms.tellPlayer(comms.t("pending_action_section", comms.t("no_pending_action")));
        } else {
            SignEditInteraction interaction = interactionManager.getPendingInteraction(player);
            comms.tellPlayer(comms.t("pending_action_section", comms.t(interaction.getName())));
            comms.tellPlayer(interaction.getActionHint(comms));
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

        comms.tellPlayer(comms.t("history_section",
                comms.t("history_have", undosRemaining, redosRemaining)
        ));
    }

    private void reportClipboard() {
        SignText clipboard = clipboardManager.getClipboard(player);
        if (clipboard == null) {
            comms.tellPlayer(comms.t("clipboard_contents_section", comms.t("empty_clipboard")));
        } else {
            comms.tellPlayer(comms.t("clipboard_contents_section", ""));
            comms.dumpLines(clipboard.getLines());
        }
    }
}
