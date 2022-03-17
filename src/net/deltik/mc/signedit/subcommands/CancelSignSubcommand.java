/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class CancelSignSubcommand extends SignSubcommand {
    private final SignEditInteractionManager interactionManager;
    private final Player player;
    private final ChatComms comms;

    @Inject
    public CancelSignSubcommand(SignEditInteractionManager interactionManager, Player player, ChatComms comms) {
        this.interactionManager = interactionManager;
        this.player = player;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        SignEditInteraction interaction = interactionManager.removePendingInteraction(player);
        if (interaction == null) {
            comms.tellPlayer(comms.t("no_pending_action_to_cancel"));
        } else {
            interaction.cleanup();
            comms.tellPlayer(comms.t("cancelled_pending_action"));
        }
        return null;
    }
}
