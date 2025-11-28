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

import net.deltik.mc.signedit.subcommands.SubcommandContext;

import java.lang.reflect.InvocationTargetException;

/**
 * Factory for creating SignEditInteraction instances.
 * Uses reflection to instantiate interactions with a standard {@link SubcommandContext} constructor.
 */
public class InteractionFactory {

    public SignEditInteraction create(Class<? extends SignEditInteraction> interactionClass, SubcommandContext context) {
        try {
            return interactionClass.getConstructor(SubcommandContext.class).newInstance(context);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new IllegalArgumentException("Cannot create interaction: " + interactionClass.getName(), e);
        }
    }
}