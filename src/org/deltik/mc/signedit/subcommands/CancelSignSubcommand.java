/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class CancelSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private final Player player;
    private final ChatComms comms;

    @Inject
    public CancelSignSubcommand(SignEditListener listener, Player player, ChatComms comms) {
        this.listener = listener;
        this.player = player;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        SignEditInteraction interaction = listener.removePendingInteraction(player);
        if (interaction != null) {
            comms.tellPlayer(comms.t("cancelled_pending_right_click_action"));
        } else {
            comms.tellPlayer(comms.t("no_right_click_action_to_cancel"));
        }
        return null;
    }
}
