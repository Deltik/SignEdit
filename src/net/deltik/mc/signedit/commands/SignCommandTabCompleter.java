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

package net.deltik.mc.signedit.commands;

import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.LineSelectorParser;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import net.deltik.mc.signedit.subcommands.SignSubcommand;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import net.deltik.mc.signedit.subcommands.SubcommandRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SignCommandTabCompleter implements TabCompleter {
    private final SubcommandRegistry registry;
    private final Configuration config;

    public SignCommandTabCompleter(SubcommandRegistry registry, Configuration config) {
        this.registry = registry;
        this.config = config;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completion = new ArrayList<>();
        Player player = (Player) sender;

        String rawSubcommand = null;
        if (args.length >= 1) {
            rawSubcommand = args[0].toLowerCase();
        }

        if (args.length == 1) {
            completion.addAll(completeSubcommand(player, rawSubcommand));
            completion.addAll(completeLines(player, args.clone()));
        } else if (args.length == 2 && supportsLineSelector(rawSubcommand)) {
            completion.addAll(completeLines(player, args.clone()));
        }

        ArgParser argParser = new ArgParser(config, args, registry.getSubcommandNames());
        if (args.length > 1 && argParser.getRemainder().size() > 0) {
            completion.addAll(completeSubcommandArgs(player, argParser));
        }

        return completion;
    }

    private boolean supportsLineSelector(String subcommandName) {
        return registry.supportsLineSelector(subcommandName);
    }

    /**
     * Delegate to the subcommand's {@link SignSubcommand#getTabCompletions} for subcommand-specific argument completion.
     */
    private Collection<String> completeSubcommandArgs(Player player, ArgParser argParser) {
        String subcommandName = argParser.getSubcommand();
        SubcommandContext context = registry.createContext(player, argParser.getArgs());
        SignSubcommand signSubcommand = (SignSubcommand) registry.createSubcommand(subcommandName, context);
        if (signSubcommand == null) {
            return new ArrayList<>();
        }
        return signSubcommand.getTabCompletions(argParser);
    }

    protected InteractionCommand getSignSubcommand(Player player, ArgParser argParser) {
        String subcommandName = argParser.getSubcommand();
        SubcommandContext context = registry.createContext(player, new String[]{subcommandName});
        return registry.createSubcommand(subcommandName, context);
    }

    /**
     * Offer suggestions for the subcommand
     */
    private List<String> completeSubcommand(Player player, String arg) {
        Set<String> candidateSubcommands = registry.getSubcommandNames()
                .stream()
                .filter(name -> name.startsWith(arg))
                .filter(name -> {
                    ArgParser argParser = new ArgParser(config, new String[]{name}, registry.getSubcommandNames());
                    InteractionCommand signSubcommand = getSignSubcommand(player, argParser);
                    return signSubcommand != null && signSubcommand.isPermitted();
                })
                .collect(Collectors.toSet());
        return new ArrayList<>(candidateSubcommands);
    }

    /**
     * Offer suggestions for the line number selector
     */
    private List<String> completeLines(Player player, String[] args) {
        List<String> completion = new ArrayList<>();
        if (!playerIsAllowedToUseLineSelectors(player)) return completion;

        ArgParser argParser = new ArgParser(config, args, registry.getSubcommandNames());
        int minLine = config.getMinLine();
        int maxLine = config.getMaxLine();
        Pattern lineSelector = Pattern.compile("^[" + minLine + "-" + maxLine + ",\\-]+$");
        String lineSelectorArg = "";
        for (int i = 0; i < Math.min(args.length, 2); i++) {
            if (lineSelector.matcher(args[i]).matches()) {
                lineSelectorArg = args[i];
                if (args[i].endsWith("-") || args[i].endsWith(",")) {
                    args[i] = args[i].substring(0, args[i].length() - 1);
                }
                argParser = new ArgParser(config, args, registry.getSubcommandNames());
                break;
            }
        }
        // /sign version
        if (!supportsLineSelector(argParser.getSubcommand()) && args.length > 0 && !args[0].isEmpty() ||
                // /sign set
                args.length == 1 && supportsLineSelector(args[0].toLowerCase()) ||
                // /sign 2-3 NewText
                args.length == 2 && lineSelector.matcher(args[0]).matches() ||
                // /sign set bad
                args.length == 2 && !lineSelector.matcher(args[1]).matches() && !args[1].isEmpty() ||
                // /sign%20%20
                args.length == 2 && args[0].isEmpty() && args[1].isEmpty()) {
            return completion;
        }
        LineSelectorParser lineSelectorParser = new LineSelectorParser(config);
        completion.addAll(lineSelectorParser.suggestCompletion(lineSelectorArg));
        return completion;
    }

    private boolean playerIsAllowedToUseLineSelectors(Player player) {
        return registry.getSubcommandNames().stream()
                .filter(this::supportsLineSelector)
                .anyMatch(name -> {
                    ArgParser argParser = new ArgParser(config, new String[]{name}, registry.getSubcommandNames());
                    InteractionCommand signSubcommand = getSignSubcommand(player, argParser);
                    return signSubcommand != null && signSubcommand.isPermitted();
                });
    }
}
