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

import net.deltik.mc.signedit.interactions.SignEditInteraction;

/**
 * Result of executing a subcommand.
 * Describes what should happen after subcommand execution without creating the interaction directly.
 * This maintains dependency inversion - subcommands declare intent, SignCommand creates interactions.
 */
public abstract class SubcommandResult {

    /**
     * Returns a singleton result indicating no interaction is needed.
     * Used by subcommands that complete immediately (help, version, status, cancel, undo, redo).
     */
    public static SubcommandResult noInteraction() {
        return NoInteraction.INSTANCE;
    }

    /**
     * Returns a result requesting that the specified interaction be created.
     * The actual interaction creation happens in SignCommand using InteractionFactory.
     *
     * @param interactionClass The class of the SignEditInteraction to create
     */
    public static SubcommandResult requestInteraction(
            Class<? extends SignEditInteraction> interactionClass) {
        return new RequestInteraction(interactionClass);
    }

    /**
     * @return true if this result requires an interaction to be created
     */
    public abstract boolean requiresInteraction();

    /**
     * Singleton result for subcommands that don't need a sign interaction.
     */
    private static final class NoInteraction extends SubcommandResult {
        static final NoInteraction INSTANCE = new NoInteraction();

        private NoInteraction() {}

        @Override
        public boolean requiresInteraction() {
            return false;
        }
    }

    /**
     * Result requesting that a specific SignEditInteraction be created.
     */
    public static final class RequestInteraction extends SubcommandResult {
        private final Class<? extends SignEditInteraction> interactionClass;

        private RequestInteraction(Class<? extends SignEditInteraction> interactionClass) {
            this.interactionClass = interactionClass;
        }

        @Override
        public boolean requiresInteraction() {
            return true;
        }

        public Class<? extends SignEditInteraction> getInteractionClass() {
            return interactionClass;
        }
    }
}