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

import net.deltik.mc.signedit.SignEditPluginServices;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Registry for SignEdit subcommands.
 * Uses {@link GeneratedSubcommandClasses} (generated at compile time) to discover subcommands.
 */
public class SubcommandRegistry {
    private final SignEditPluginServices services;
    private final Map<String, SubcommandFactory> factories = new LinkedHashMap<>();

    @FunctionalInterface
    public interface SubcommandFactory {
        SignSubcommand create(SubcommandContext context);
    }

    public SubcommandRegistry(SignEditPluginServices services) {
        this.services = services;
        registerFromGeneratedClasses();
    }

    /**
     * Register all subcommands discovered by the annotation processor.
     */
    private void registerFromGeneratedClasses() {
        for (Map.Entry<String, Class<? extends SignSubcommand>> entry :
                GeneratedSubcommandClasses.getSubcommandClasses().entrySet()) {
            String name = entry.getKey();
            Class<? extends SignSubcommand> clazz = entry.getValue();
            factories.put(name, createFactoryForClass(clazz));
        }
    }

    /**
     * Creates a factory that instantiates the subcommand class with a SubcommandContext.
     */
    private SubcommandFactory createFactoryForClass(Class<? extends SignSubcommand> clazz) {
        return context -> {
            try {
                Constructor<? extends SignSubcommand> constructor =
                        clazz.getConstructor(SubcommandContext.class);
                return constructor.newInstance(context);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate subcommand: " + clazz.getName(), e);
            }
        };
    }

    /**
     * Returns the set of all subcommand names.
     * This delegates to the generated class for consistency.
     */
    public Set<String> getSubcommandNames() {
        return GeneratedSubcommandClasses.getSubcommandNames();
    }

    public boolean hasSubcommand(String name) {
        return GeneratedSubcommandClasses.hasSubcommand(name);
    }

    public boolean supportsLineSelector(String name) {
        return GeneratedSubcommandClasses.supportsLineSelector(name);
    }

    /**
     * Create a subcommand instance for the given name and context.
     *
     * @param name    The subcommand name (e.g., "set", "help")
     * @param context The execution context containing player, args, and services
     * @return The subcommand instance, or null if not found
     */
    public InteractionCommand createSubcommand(String name, SubcommandContext context) {
        SubcommandFactory factory = factories.get(name);
        if (factory == null) {
            return null;
        }
        return factory.create(context);
    }

    /**
     * Create a SubcommandContext for the given player and args.
     */
    public SubcommandContext createContext(Player player, String[] args) {
        return new SubcommandContext(player, args, services, getSubcommandNames(), this::createSubcommand);
    }
}
