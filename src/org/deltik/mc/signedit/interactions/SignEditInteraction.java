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

package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.deltik.mc.signedit.ChatComms;
import org.jetbrains.annotations.NotNull;

public interface SignEditInteraction {
    void interact(Player player, Sign sign);

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default String getActionHint(ChatComms comms) {
        return comms.t("right_click_sign_to_apply_action_hint");
    }

    default void cleanup(Event event) {
    }

    default void cleanup() {
        cleanup(new Event() {
            @NotNull
            @Override
            public HandlerList getHandlers() {
                return new HandlerList();
            }
        });
    }
}
