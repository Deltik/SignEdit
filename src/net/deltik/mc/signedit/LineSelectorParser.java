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

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.exceptions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LineSelectorParser {
    public static final int[] NO_LINES_SELECTED = new int[0];
    public static final int[] ALL_LINES_SELECTED = new int[]{0, 1, 2, 3};
    public static final String RANGE_SEPARATOR = "-";
    public static final String GROUP_SEPARATOR = ",";
    private final Configuration config;

    public LineSelectorParser(Configuration config) {
        this.config = config;
    }

    public boolean isLineSelection(String input) {
        try {
            toSelectedLines(input);
            return true;
        } catch (LineSelectionException ignored) {
            return false;
        }
    }

    public List<String> suggestCompletion(String input) {
        List<String> completion = new ArrayList<>();

        if (input.isEmpty()) {
            return Arrays.stream(ALL_LINES_SELECTED)
                    .mapToObj(value -> String.valueOf(value + config.getLineStartsAt()))
                    .collect(Collectors.toList());
        }

        try {
            if (input.endsWith(RANGE_SEPARATOR)) {
                return suggestRangeCompletion(input);
            } else if (input.endsWith(GROUP_SEPARATOR)) {
                return suggestGroupCompletion(input);
            }

            List<Integer> availableLines = getAllLinesSelectedAsList();
            availableLines.removeAll(Arrays.stream(toSelectedLines(input)).boxed().collect(Collectors.toList()));

            if (suggestGroupCompletion(input + GROUP_SEPARATOR).size() > 0) {
                completion.add(input + GROUP_SEPARATOR);
            }
            if (suggestRangeCompletion(input + RANGE_SEPARATOR).size() > 0) {
                completion.add(input + RANGE_SEPARATOR);
            }
        } catch (LineSelectionException ignored) {
        }

        return completion;
    }

    private List<Integer> getSelectedLinesSoFar(String input) {
        int indexOfLastGroupSeparator = input.lastIndexOf(GROUP_SEPARATOR);
        List<Integer> linesSelectedSoFar;
        if (indexOfLastGroupSeparator >= 0) {
            String previousGroups = input.substring(0, indexOfLastGroupSeparator);
            linesSelectedSoFar = Arrays.stream(toSelectedLines(previousGroups))
                    .boxed()
                    .collect(Collectors.toList());
        } else {
            linesSelectedSoFar = new ArrayList<>();
        }
        return linesSelectedSoFar;
    }

    private List<String> suggestGroupCompletion(String input) {
        int lineStartsAt = config.getLineStartsAt();

        List<Integer> availableLines = getAllLinesSelectedAsList();
        availableLines.removeAll(getSelectedLinesSoFar(input));

        return availableLines
                .stream()
                .map(line -> input + (line + lineStartsAt))
                .collect(Collectors.toList());
    }

    private List<Integer> getAllLinesSelectedAsList() {
        return Arrays.stream(ALL_LINES_SELECTED)
                .boxed()
                .collect(Collectors.toList());
    }

    private List<String> suggestRangeCompletion(String input) {
        int lineStartsAt = config.getLineStartsAt();
        int maxLine = config.getMaxLine();

        List<String> completion = new ArrayList<>();

        int indexOfLastGroupSeparator = input.lastIndexOf(GROUP_SEPARATOR);
        List<Integer> selectedLinesSoFar = getSelectedLinesSoFar(input);
        try {
            int rangeStart = Integer.parseInt(
                    input.substring(
                            indexOfLastGroupSeparator + 1,
                            input.length() - RANGE_SEPARATOR.length()
                    )
            );
            for (++rangeStart; rangeStart <= maxLine; rangeStart++) {
                if (selectedLinesSoFar.contains(rangeStart - lineStartsAt)) break;
                completion.add(input + rangeStart);
            }
        } catch (NumberFormatException ignored) {
        }

        return completion;
    }

    public int[] toSelectedLines(String input) {
        byte selectedLinesMask = 0;
        String[] lineRanges = input.split(GROUP_SEPARATOR, -1);
        for (String lineRange : lineRanges) {
            if (lineRange.startsWith(RANGE_SEPARATOR)) {
                parseLineNumberFromString(lineRange);
            }
            String[] lineRangeSplit = lineRange.split(RANGE_SEPARATOR, -1);
            if (lineRangeSplit.length == 2) {
                int lowerBound = parseLineNumberFromString(lineRangeSplit[0]);
                int upperBound = parseLineNumberFromString(lineRangeSplit[1]);
                if (lowerBound > upperBound) {
                    throw new RangeOrderLineSelectionException(input, lineRangeSplit[0], lineRangeSplit[1]);
                }
                for (int i = lowerBound; i <= upperBound; i++) {
                    selectedLinesMask |= 1 << i;
                }
            } else if (lineRangeSplit.length == 1) {
                int lineNumber = parseLineNumberFromString(lineRange);
                selectedLinesMask |= 1 << lineNumber;
            } else if (lineRangeSplit.length > 2) {
                throw new RangeParseLineSelectionException(input, lineRange);
            }
        }

        int[] selectedLines = new int[Integer.bitCount(selectedLinesMask)];
        int selectedLinesPosition = 0;
        for (int i = 0; i <= (config.getMaxLine() - config.getMinLine()); i++) {
            if ((selectedLinesMask >>> i & 1) == 0x1) {
                selectedLines[selectedLinesPosition++] = i;
            }
        }
        return selectedLines;
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
