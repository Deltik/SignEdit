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
import net.deltik.mc.signedit.SignEditPluginServices;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * Holds per-invocation context for subcommand execution.
 * Created fresh for each command invocation.
 */
public class SubcommandContext {
    private final Player player;
    private final String[] args;
    private final SignEditPluginServices services;
    private final Set<String> subcommandNames;
    private final BiFunction<String, SubcommandContext, InteractionCommand> subcommandFactory;

    private ArgParser argParser;
    private SignText signText;

    public SubcommandContext(
            Player player,
            String[] args,
            SignEditPluginServices services,
            Set<String> subcommandNames,
            BiFunction<String, SubcommandContext, InteractionCommand> subcommandFactory
    ) {
        this.player = player;
        this.args = args;
        this.services = services;
        this.subcommandNames = subcommandNames;
        this.subcommandFactory = subcommandFactory;
    }

    public Player player() {
        return player;
    }

    public String[] args() {
        return args;
    }

    public SignEditPluginServices services() {
        return services;
    }

    public Set<String> subcommandNames() {
        return subcommandNames;
    }

    /**
     * Gets the ArgParser for this invocation, creating it lazily if needed.
     */
    public ArgParser argParser() {
        if (argParser == null) {
            argParser = new ArgParser(services.config(), args, subcommandNames);
        }
        return argParser;
    }

    /**
     * Gets a fresh SignText instance for this invocation.
     * Each call creates a new instance, as SignText is mutable and per-interaction.
     */
    public SignText createSignText() {
        return new SignText(services.signEditValidator());
    }

    /**
     * Gets the shared SignText instance for this context.
     * Use this when the same SignText should be shared across subcommand and interaction.
     */
    public SignText signText() {
        if (signText == null) {
            signText = createSignText();
        }
        return signText;
    }

    /**
     * Creates a subcommand instance for permission checking.
     * This creates a fresh SubcommandContext with minimal args for the subcommand.
     *
     * @param name The subcommand name
     * @return The subcommand instance, or null if the subcommand doesn't exist
     */
    public InteractionCommand createSubcommandForPermissionCheck(String name) {
        if (subcommandFactory == null) {
            return null;
        }
        SubcommandContext minimalContext = new SubcommandContext(
                player,
                new String[]{name},
                services,
                subcommandNames,
                subcommandFactory
        );
        return subcommandFactory.apply(name, minimalContext);
    }

    /**
     * Creates a minimal SubcommandContext for interactions triggered by listeners
     * (e.g., click-to-edit) that don't go through the command system.
     *
     * @param player   The player triggering the interaction
     * @param services The plugin services
     * @param signText The SignText to use for the interaction
     * @return A minimal SubcommandContext suitable for listener-created interactions
     */
    public static SubcommandContext forListener(
            Player player,
            SignEditPluginServices services,
            SignText signText
    ) {
        SubcommandContext context = new SubcommandContext(
                player,
                new String[0],
                services,
                null,
                null
        );
        context.signText = signText;
        return context;
    }
}
