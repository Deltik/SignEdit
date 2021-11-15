/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.exceptions.*;
import net.deltik.mc.signedit.subcommands.SubcommandName;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ArgParser {
    private final Configuration config;
    private final Set<String> subcommandNames;

    public static final int[] NO_LINES_SELECTED = new int[0];
    public static final int[] ALL_LINES_SELECTED = new int[]{0, 1, 2, 3};

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
        byte selectedLinesMask = 0;
        String[] lineRanges = rawLineGroups.split(",", -1);
        for (String lineRange : lineRanges) {
            if (lineRange.startsWith("-")) {
                parseLineNumberFromString(lineRange);
            }
            String[] lineRangeSplit = lineRange.split("-");
            if (lineRangeSplit.length == 2) {
                int lowerBound = parseLineNumberFromString(lineRangeSplit[0]);
                int upperBound = parseLineNumberFromString(lineRangeSplit[1]);
                if (lowerBound > upperBound) {
                    throw new RangeOrderLineSelectionException(rawLineGroups, lineRangeSplit[0], lineRangeSplit[1]);
                }
                for (int i = lowerBound; i <= upperBound; i++) {
                    selectedLinesMask |= 1 << i;
                }
            } else if (lineRangeSplit.length == 1) {
                int lineNumber = parseLineNumberFromString(lineRange);
                selectedLinesMask |= 1 << lineNumber;
            } else if (lineRangeSplit.length > 2) {
                throw new RangeParseLineSelectionException(rawLineGroups, lineRange);
            }
        }

        this.selectedLines = new int[Integer.bitCount(selectedLinesMask)];
        int selectedLinesPosition = 0;
        for (int i = 0; i <= (config.getMaxLine() - config.getMinLine()); i++) {
            if ((selectedLinesMask >>> i & 1) == 0x1) {
                this.selectedLines[selectedLinesPosition++] = i;
            }
        }
    }

    private int parseLineNumberFromString(String rawLineNumber) {
        int unadjustedLineNumber;
        try {
            unadjustedLineNumber = Integer.parseInt(rawLineNumber);
        } catch (NumberFormatException e) {
            throw new NumberParseLineSelectionException(rawLineNumber);
        }
        if (unadjustedLineNumber > config.getMaxLine() ||
                unadjustedLineNumber < config.getMinLine()) {
            throw new OutOfBoundsLineSelectionException(rawLineNumber);
        }
        return unadjustedLineNumber - config.getMinLine();
    }
}
