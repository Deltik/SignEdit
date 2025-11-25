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

import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class SignSubcommand extends Subcommand implements InteractionCommand {
    private final SubcommandContext context;

    protected SignSubcommand(SubcommandContext context) {
        this.context = context;
    }

    /**
     * Get the execution context for this subcommand.
     */
    protected SubcommandContext context() {
        return context;
    }

    /**
     * Get the player executing this subcommand.
     */
    protected Player player() {
        return context.player();
    }

    /**
     * Get the argument parser for this subcommand.
     */
    protected ArgParser argParser() {
        return context.argParser();
    }

    @Override
    public boolean isPermitted() {
        return  // Legacy (< 1.4) permissions
                player().hasPermission("signedit.use") ||
                        // /sign <subcommand>
                        player().hasPermission("signedit." + SignCommand.COMMAND_NAME + "." + getName());
    }

    /**
     * Get the subcommand name.
     * This can be overridden by subclasses, but the default derives it from the class name.
     */
    public String getName() {
        String simpleClassName = getClass().getSimpleName();
        int end = simpleClassName.lastIndexOf(SignSubcommand.class.getSimpleName());
        if (end != -1) {
            return simpleClassName.substring(0, end).toLowerCase();
        }
        return simpleClassName.toLowerCase();
    }

    /**
     * Get tab completions for this subcommand.
     * Override in subclasses to provide custom completions.
     *
     * @param argParser The argument parser with current input
     * @return List of tab completion suggestions
     */
    public List<String> getTabCompletions(ArgParser argParser) {
        return Collections.emptyList();
    }
}
