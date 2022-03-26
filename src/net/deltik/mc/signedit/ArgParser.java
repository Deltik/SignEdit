/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.LineSelectionException;
import net.deltik.mc.signedit.exceptions.NumberParseLineSelectionException;
import net.deltik.mc.signedit.subcommands.SubcommandName;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static net.deltik.mc.signedit.LineSelectorParser.NO_LINES_SELECTED;

@Deprecated
public class ArgParser {
    private final Configuration config;
    private final Set<String> subcommandNames;

    String subcommand;
    int[] selectedLines = NO_LINES_SELECTED;
    LineSelectionException selectedLinesError;
    List<String> remainder;

    @Inject
    public ArgParser(
            Configuration config,
            @ArgParserArgs String[] args,
            @SubcommandName Set<String> subcommandNames
    ) {
        this.config = config;
        this.subcommandNames = subcommandNames;
        parseArgs(args);
    }

    public String getSubcommand() {
        return subcommand;
    }

    public int[] getLinesSelection() {
        return selectedLines;
    }

    public LineSelectionException getLinesSelectionError() {
        return selectedLinesError;
    }

    public List<String> getRemainder() {
        return remainder;
    }

    private void parseArgs(String[] args) {
        List<String> argsArray = new LinkedList<>(Arrays.asList(args));
        selectedLines = NO_LINES_SELECTED;
        remainder = argsArray;

        if (argsArray.size() == 0) {
            subcommand = SignCommand.SUBCOMMAND_NAME_HELP;
            return;
        }

        String maybeSubcommandOrShorthandLines = remainder.remove(0);
        String maybeSubcommand = maybeSubcommandOrShorthandLines.toLowerCase();
        if (subcommandNames.contains(maybeSubcommand)) {
            subcommand = maybeSubcommand;
        }
        if (subcommand == null) {
            try {
                parseLineSelection(maybeSubcommandOrShorthandLines);
                if (selectedLines.length > 0) {
                    if (remainder.size() == 0) subcommand = "clear";
                    else subcommand = "set";
                }
                return;
            } catch (NumberParseLineSelectionException e) {
                remainder.add(0, maybeSubcommandOrShorthandLines);
            } catch (LineSelectionException e) {
                remainder.add(0, maybeSubcommandOrShorthandLines);
                subcommand = "set";
            }
        }
        if (subcommand == null) {
            subcommand = SignCommand.SUBCOMMAND_NAME_HELP;
        }
        if (remainder.size() == 0 || subcommand.equals(SignCommand.SUBCOMMAND_NAME_HELP)) {
            return;
        }

        try {
            parseLineSelection(remainder.get(0));
            remainder.remove(0);
        } catch (LineSelectionException e) {
            selectedLines = NO_LINES_SELECTED;
            selectedLinesError = e;
        }
    }

    private void parseLineSelection(String rawLineGroups) {
        LineSelectorParser lineSelectorParser = new LineSelectorParser(config);
        this.selectedLines = lineSelectorParser.toSelectedLines(rawLineGroups);
    }
}
