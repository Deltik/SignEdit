/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit;

import org.deltik.mc.signedit.exceptions.*;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArgParser {
    private final Configuration config;
    private final Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> subcommandMap;

    public static final int[] NO_LINES_SELECTED = new int[0];
    public static final int[] ALL_LINES_SELECTED = new int[]{0, 1, 2, 3};

    String subcommand;
    int[] selectedLines = NO_LINES_SELECTED;
    LineSelectionException selectedLinesError;
    List<String> remainder;

    @Inject
    public ArgParser(
            String[] args,
            Configuration config,
            Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> subcommandMap
    ) {
        this.config = config;
        this.subcommandMap = subcommandMap;
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

        if (argsArray.size() <= 0) {
            subcommand = "help";
            selectedLines = NO_LINES_SELECTED;
            remainder = argsArray;
            return;
        }

        if (subcommandMap.containsKey(argsArray.get(0).toLowerCase())) {
            subcommand = argsArray.remove(0).toLowerCase();
        }
        if (argsArray.size() <= 0) {
            selectedLines = NO_LINES_SELECTED;
            remainder = argsArray;
            return;
        }

        try {
            parseLineSelection(argsArray.get(0));
            argsArray.remove(0);
        } catch (LineSelectionException e) {
            selectedLines = NO_LINES_SELECTED;
            selectedLinesError = e;
        }

        remainder = argsArray;

        if (subcommand == null && selectedLines.length > 0) {
            if (remainder.size() == 0) subcommand = "clear";
            else subcommand = "set";
        }

        if (subcommand == null) subcommand = "help";
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
