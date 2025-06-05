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

import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import org.bukkit.entity.Player;

public abstract class SignSubcommand extends Subcommand implements InteractionCommand {
    private final Player player;

    protected SignSubcommand(Player player) {
        this.player = player;
    }

    @Override
    public boolean isPermitted() {
        return  // Legacy (< 1.4) permissions
                player.hasPermission("signedit.use") ||
                        // /sign <subcommand>
                        player.hasPermission("signedit." + SignCommand.COMMAND_NAME + "." + getName());
    }

    /**
     * Get the subcommand name
     */
    private String getName() {
        String simpleClassName = getClass().getSimpleName();
        int end = simpleClassName.lastIndexOf(SignSubcommand.class.getSimpleName());
        if (end != -1) {
            return simpleClassName.substring(0, end).toLowerCase();
        }
        return simpleClassName.toLowerCase();
    }
}
