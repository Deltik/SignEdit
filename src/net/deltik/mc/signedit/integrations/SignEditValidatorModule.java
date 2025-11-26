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

package net.deltik.mc.signedit.integrations;

import org.bukkit.plugin.PluginManager;

/**
 * Factory for creating SignEditValidator instances.
 * Replaces Dagger module with simple static factory methods.
 */
public class SignEditValidatorModule {
    private SignEditValidatorModule() {
        // Utility class
    }

    /**
     * Creates a SignEditValidator based on configuration.
     *
     * @param validationType The type of validation: "standard", "extra", or "none"
     * @param pluginManager  The Bukkit PluginManager for event dispatching
     * @return The appropriate SignEditValidator implementation
     */
    public static SignEditValidator provideSignEditValidator(String validationType, PluginManager pluginManager) {
        switch (validationType.toLowerCase()) {
            case "extra":
                return new BreakReplaceSignEditValidator(pluginManager);
            case "none":
                return new NoopSignEditValidator();
            case "standard":
            default:
                return new StandardSignEditValidator(pluginManager);
        }
    }
}
