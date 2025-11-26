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
import net.deltik.mc.signedit.interactions.SignEditInteraction;

@SignSubcommandInfo(name = "cancel")
public class CancelSignSubcommand extends SignSubcommand {
    public CancelSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        ChatComms comms = context().services().chatCommsFactory().create(player());

        SignEditInteraction interaction = context().services().interactionManager()
                .removePendingInteraction(player());
        if (interaction == null) {
            comms.tell(comms.t("no_pending_action_to_cancel"));
        } else {
            interaction.cleanup();
            comms.tell(comms.t("cancelled_pending_action"));
        }
        return null;
    }
}
