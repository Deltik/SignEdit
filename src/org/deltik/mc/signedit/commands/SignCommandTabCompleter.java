/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit.commands;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SignCommandTabCompleter implements TabCompleter {
    private final Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> subcommandMap;
    private final Set<String> subcommandNames;
    private final Configuration config;
    private final Set<String> subcommandsWithLineSelector = Stream.of(
            "set",
            "clear",
            "copy",
            "cut"
    ).collect(Collectors.toSet());

    @Inject
    public SignCommandTabCompleter(
            Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders,
            Configuration config
    ) {
        subcommandMap = commandBuilders;
        subcommandNames = commandBuilders.keySet();
        this.config = config;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completion = new ArrayList<>();

        if (args.length == 1) {
            completion.addAll(completeSubcommand(args[0].toLowerCase()));
            completion.addAll(completeLines(args.clone()));
        } else if (args.length == 2) {
            completion.addAll(completeLines(args.clone()));
        }

        completion.addAll(completeExistingLines((Player) sender, args));

        return completion;
    }

    private List<String> completeExistingLines(Player player, String[] args) {
        List<String> nothing = new ArrayList<>();

        ArgParser argParser = new ArgParser(args, config, subcommandMap);
        if (!"set".equals(argParser.getSubcommand()) || argParser.getRemainder().size() <= 0) {
            return nothing;
        }

        Block targetBlock = SignCommand.getTargetBlockOfPlayer(player);
        BlockState targetBlockState = targetBlock.getState();
        if (targetBlockState instanceof Sign) {
            Sign targetSign = (Sign) targetBlockState;
            SignText signText = new SignText();
            signText.setTargetSign(targetSign);
            signText.importSign();
            List<String> qualifyingLines = new ArrayList<>();
            for (int selectedLine : argParser.getLinesSelection()) {
                qualifyingLines.add(signText.getLineParsed(selectedLine));
            }
            return qualifyingLines.stream()
                    .filter(
                            line -> line.startsWith(String.join(" ", argParser.getRemainder()))
                    )
                    .collect(Collectors.toList());
        }

        return nothing;
    }

    /**
     * Offer suggestions for the subcommand
     */
    private List<String> completeSubcommand(String arg) {
        Set<String> candidateSubcommands = subcommandNames;
        candidateSubcommands = candidateSubcommands
                .stream()
                .filter(
                        name -> name.startsWith(arg)
                )
                .collect(Collectors.toSet());
        return new ArrayList<>(candidateSubcommands);
    }

    /**
     * Offer suggestions for the line number selector
     * <p>
     * FIXME: This code is disgusting.
     */
    private List<String> completeLines(String[] args) {
        ArgParser argParser = new ArgParser(args, config, subcommandMap);
        List<String> completion = new ArrayList<>();
        int minLine = config.getMinLine();
        int maxLine = config.getMaxLine();
        Pattern lineSelector = Pattern.compile("^[" + minLine + "-" + maxLine + ",\\-]+$");
        String lineSelectorArg = null;
        boolean doLineSelectorCompletion = false;
        for (int i = 0; i < Math.min(args.length, 2); i++) {
            if (lineSelector.matcher(args[i]).matches()) {
                lineSelectorArg = args[i];
                if (args[i].endsWith("-") || args[i].endsWith(",")) {
                    args[i] = args[i].substring(0, args[i].length() - 1);
                }
                argParser = new ArgParser(args, config, subcommandMap);
                doLineSelectorCompletion = true;
                break;
            }
        }
        // /sign version
        if (!subcommandsWithLineSelector.contains(argParser.getSubcommand()) && args.length > 0 && !args[0].isEmpty() ||
                // /sign set
                args.length == 1 && subcommandsWithLineSelector.contains(args[0].toLowerCase()) ||
                // /sign 2-3 NewText
                args.length == 2 && lineSelector.matcher(args[0]).matches() ||
                // /sign set bad
                args.length == 2 && !lineSelector.matcher(args[1]).matches() && !args[1].isEmpty() ||
                // /sign%20%20
                args.length == 2 && args[0].isEmpty() && args[1].isEmpty()) {
            return completion;
        }
        if (doLineSelectorCompletion) {
            int[] linesSelectedSoFar = argParser.getLinesSelection();
            if (linesSelectedSoFar == ArgParser.ALL_LINES_SELECTED
                    || linesSelectedSoFar == ArgParser.NO_LINES_SELECTED) {
                return completion;
            }
            List<Integer> availableLines = Arrays.stream(ArgParser.ALL_LINES_SELECTED)
                    .boxed()
                    .collect(Collectors.toList());
            Set<Integer> selectedLines = new HashSet<>();
            for (int selectedLine : linesSelectedSoFar) {
                selectedLines.add(selectedLine);
            }
            availableLines.removeAll(selectedLines);
            String[] lineGroup = lineSelectorArg.split(",");
            String lineGroupLast = lineGroup[lineGroup.length - 1];
            if (lineSelectorArg.endsWith("-")) {
                try {
                    int lastLineGroupLowerBound = Integer.parseInt(
                            lineGroupLast.substring(0, lineGroupLast.length() - 1)
                    );
                    for (++lastLineGroupLowerBound; lastLineGroupLowerBound <= maxLine; lastLineGroupLowerBound++) {
                        if (selectedLines.contains(lastLineGroupLowerBound - minLine)) break;
                        completion.add(lineSelectorArg + lastLineGroupLowerBound);
                    }
                } catch (NumberFormatException e) {
                    return completion;
                }
            } else if (lineSelectorArg.endsWith(",")) {
                String finalLineSelectorArg = lineSelectorArg;
                completion.addAll(availableLines.stream().map(
                        line -> finalLineSelectorArg + (line + minLine)
                ).collect(Collectors.toList()));
            } else if (availableLines.size() > 0) {
                completion.add(lineSelectorArg + ",");
                if (!lineGroupLast.contains("-")) {
                    try {
                        int lastLineGroupLowerBound = Integer.parseInt(lineGroupLast);
                        if (!selectedLines.contains(lastLineGroupLowerBound - minLine + 1)
                                && lastLineGroupLowerBound < maxLine) {
                            completion.add(lineSelectorArg + "-");
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } else {
            for (int line = minLine; line <= maxLine; line++) {
                completion.add(String.valueOf(line));
            }
        }
        return completion;
    }
}
