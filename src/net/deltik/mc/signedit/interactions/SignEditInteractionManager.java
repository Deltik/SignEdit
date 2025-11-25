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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

public class SignEditInteractionManager {
    protected ChatCommsFactory chatCommsFactory;
    protected final Map<Player, SignEditInteraction> pendingInteractions = new HashMap<>();

    public SignEditInteractionManager() {
    }

    public void setChatCommsFactory(ChatCommsFactory chatCommsFactory) {
        this.chatCommsFactory = chatCommsFactory;
    }

    public void endInteraction(Player player, Event event) {
        try {
            removePendingInteraction(player).cleanup(event);
        } catch (Throwable e) {
            if (chatCommsFactory != null) {
                ChatComms comms = chatCommsFactory.create(player);
                comms.reportException(e);
            }
        }
    }

    public void setPendingInteraction(Player player, SignEditInteraction interaction) {
        if (pendingInteractions.get(player) != null) {
            removePendingInteraction(player).cleanup();
        }
        pendingInteractions.put(player, interaction);
    }

    public boolean isInteractionPending(Player player) {
        return pendingInteractions.containsKey(player);
    }

    public SignEditInteraction removePendingInteraction(Player player) {
        return pendingInteractions.remove(player);
    }

    public SignEditInteraction getPendingInteraction(Player player) {
        return pendingInteractions.get(player);
    }
}
