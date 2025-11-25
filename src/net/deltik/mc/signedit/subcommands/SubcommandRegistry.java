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

import java.util.*;

/**
 * Registry for SignEdit subcommands.
 * Replaces the Dagger-based SignSubcommandComponent and SignCommandModule.
 */
public class SubcommandRegistry {
    private final SignEditPluginServices services;
    private final Map<String, SubcommandFactory> factories = new LinkedHashMap<>();
    private final Map<String, SignSubcommandInfo> metadata = new HashMap<>();

    @FunctionalInterface
    public interface SubcommandFactory {
        SignSubcommand create(SubcommandContext context);
    }

    public SubcommandRegistry(SignEditPluginServices services) {
        this.services = services;
        registerBuiltInSubcommands();
    }

    private void registerBuiltInSubcommands() {
        register("help", HelpSignSubcommand.class, HelpSignSubcommand::new);
        register("set", SetSignSubcommand.class, SetSignSubcommand::new);
        register("clear", ClearSignSubcommand.class, ClearSignSubcommand::new);
        register("ui", UiSignSubcommand.class, UiSignSubcommand::new);
        register("cancel", CancelSignSubcommand.class, CancelSignSubcommand::new);
        register("status", StatusSignSubcommand.class, StatusSignSubcommand::new);
        register("copy", CopySignSubcommand.class, CopySignSubcommand::new);
        register("cut", CutSignSubcommand.class, CutSignSubcommand::new);
        register("paste", PasteSignSubcommand.class, PasteSignSubcommand::new);
        register("undo", UndoSignSubcommand.class, UndoSignSubcommand::new);
        register("redo", RedoSignSubcommand.class, RedoSignSubcommand::new);
        register("unwax", UnwaxSignSubcommand.class, UnwaxSignSubcommand::new);
        register("wax", WaxSignSubcommand.class, WaxSignSubcommand::new);
        register("version", VersionSignSubcommand.class, VersionSignSubcommand::new);
    }

    private void register(String name, Class<? extends SignSubcommand> clazz, SubcommandFactory factory) {
        factories.put(name, factory);
        SignSubcommandInfo info = clazz.getAnnotation(SignSubcommandInfo.class);
        if (info != null) {
            metadata.put(name, info);
        }
    }

    public Set<String> getSubcommandNames() {
        return Collections.unmodifiableSet(factories.keySet());
    }

    public boolean hasSubcommand(String name) {
        return factories.containsKey(name);
    }

    public SignSubcommandInfo getMetadata(String name) {
        return metadata.get(name);
    }

    public boolean supportsLineSelector(String name) {
        SignSubcommandInfo info = metadata.get(name);
        return info != null && info.supportsLineSelector();
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
