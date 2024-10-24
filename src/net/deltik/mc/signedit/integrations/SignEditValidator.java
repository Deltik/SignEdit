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

package net.deltik.mc.signedit.integrations;

import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public interface SignEditValidator {
    /**
     * Ensure that the changed sign passes checks before saving
     *
     * @param proposedSign A changed sign that has not had {@link Sign#update()} called yet
     * @param side         The side of the sign that has staged changes
     * @param player       The player that is trying to edit the sign
     */
    void validate(SignShim proposedSign, SideShim side, Player player);

    /**
     * Import sign changes from a {@link SignChangeEvent} and ensure that they pass checks before saving
     *
     * @param signChangeEvent A sign change event in the validation phase (i.e. not passed in by an {@link EventHandler}
     *                        with {@link EventPriority#MONITOR})
     */
    void validate(SignChangeEvent signChangeEvent);
}
