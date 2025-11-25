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

/**
 * Factory for creating SignEditValidator instances.
 * Replaces Dagger module with simple static factory methods.
 */
public class SignEditValidatorModule {
    private SignEditValidatorModule() {
        // Utility class
    }

    /**
     * Creates the default SignEditValidator (StandardSignEditValidator).
     * This is used when no configuration is available or for simple cases.
     */
    public static SignEditValidator provideSignEditValidator() {
        return new StandardSignEditValidator();
    }

    /**
     * Creates a SignEditValidator based on configuration.
     *
     * @param validationType The type of validation: "standard", "extra", or "none"
     * @return The appropriate SignEditValidator implementation
     */
    public static SignEditValidator provideSignEditValidator(String validationType) {
        switch (validationType.toLowerCase()) {
            case "extra":
                return new BreakReplaceSignEditValidator();
            case "none":
                return new NoopSignEditValidator();
            case "standard":
            default:
                return new StandardSignEditValidator();
        }
    }
}
