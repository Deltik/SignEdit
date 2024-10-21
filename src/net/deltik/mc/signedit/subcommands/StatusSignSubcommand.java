/*
 * Copyright (C) 2017-2024 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit.subcommands;

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class StatusSignSubcommand extends SignSubcommand {
    private final Player player;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignEditInteractionManager interactionManager;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;

    @Inject
    public StatusSignSubcommand(
            Player player,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignEditInteractionManager interactionManager,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager
    ) {
        super(player);
        this.player = player;
        this.commsBuilder = commsBuilder;
        this.interactionManager = interactionManager;
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        reportHistory(comms);
        reportPendingAction(comms);
        reportClipboard(comms);

        return null;
    }

    private void reportPendingAction(ChatComms comms) {
        if (!interactionManager.isInteractionPending(player)) {
            comms.tell(comms.t("pending_action_section", comms.t("no_pending_action")));
        } else {
            SignEditInteraction interaction = interactionManager.getPendingInteraction(player);
            comms.tell(comms.t("pending_action_section", comms.t(interaction.getName())));
            comms.tell(interaction.getActionHint(comms));
        }
    }

    private void reportHistory(ChatComms comms) {
        SignTextHistory history = historyManager.getHistory(player);
        int undosRemaining = 0;
        int redosRemaining = 0;

        if (history != null) {
            undosRemaining = history.undosRemaining();
            redosRemaining = history.redosRemaining();
        }

        comms.tell(comms.t("history_section",
                comms.t("history_have", undosRemaining, redosRemaining)
        ));
    }

    private void reportClipboard(ChatComms comms) {
        SignText clipboard = clipboardManager.getClipboard(player);
        if (clipboard == null) {
            comms.tell(comms.t("clipboard_contents_section", comms.t("empty_clipboard")));
        } else {
            comms.tell(comms.t("clipboard_contents_section", ""));
            comms.dumpLines(clipboard.getLines());
        }
    }
}
