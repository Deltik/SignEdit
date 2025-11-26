/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistory;
import net.deltik.mc.signedit.interactions.SignEditInteraction;

@SignSubcommandInfo(name = "status")
public class StatusSignSubcommand extends SignSubcommand {
    public StatusSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        ChatComms comms = context().services().chatCommsFactory().create(player());

        reportHistory(comms);
        reportPendingAction(comms);
        reportClipboard(comms);

        return null;
    }

    private void reportPendingAction(ChatComms comms) {
        if (!context().services().interactionManager().isInteractionPending(player())) {
            comms.tell(comms.t("pending_action_section", comms.t("no_pending_action")));
        } else {
            SignEditInteraction interaction = context().services().interactionManager()
                    .getPendingInteraction(player());
            comms.tell(comms.t("pending_action_section", comms.t(interaction.getName())));
            comms.tell(interaction.getActionHint(comms));
        }
    }

    private void reportHistory(ChatComms comms) {
        SignTextHistory history = context().services().historyManager().getHistory(player());
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
        SignText clipboard = context().services().clipboardManager().getClipboard(player());
        if (clipboard == null) {
            comms.tell(comms.t("clipboard_contents_section", comms.t("empty_clipboard")));
        } else {
            comms.tell(comms.t("clipboard_contents_section", ""));
            comms.dumpLines(clipboard.getLines());
        }
    }
}
