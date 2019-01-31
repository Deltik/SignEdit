package org.deltik.mc.signedit;

import org.deltik.mc.signedit.subcommands.SignSubcommand;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArgParser {
    private final Configuration config;
    private final Map<String, Provider<SignSubcommand>> subcommandMap;

    private static final int[] NO_LINES_SELECTED = new int[0];

    String subcommand;
    int[] selectedLines = NO_LINES_SELECTED;
    Exception selectedLinesError;
    List<String> remainder;

    @Inject
    public ArgParser(String[] args, Configuration config, Map<String, Provider<SignSubcommand>> subcommandMap) {
        this.config = config;
        this.subcommandMap = subcommandMap;
        parseArgs(args);
    }

    public String getSubcommand() {
        return subcommand;
    }

    public Provider<SignSubcommand> getSubcommandProvider() {
        return subcommandMap.get(subcommand);
    }

    public int[] getSelectedLines() {
        return selectedLines;
    }

    public Exception getSelectedLinesError() {
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

        if (subcommandMap.containsKey(argsArray.get(0))) {
            subcommand = argsArray.remove(0);
        }
        if (argsArray.size() <= 0) {
            selectedLines = NO_LINES_SELECTED;
            remainder = argsArray;
            return;
        }

        try {
            parseLineSelection(argsArray.get(0));
            argsArray.remove(0);
        } catch (IllegalArgumentException e) {
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
        String[] linesGroup = rawLineGroups.split(",");
        for (String lineRange : linesGroup) {
            String[] lineRangeSplit = lineRange.split("-");
            if (lineRangeSplit.length == 2) {
                int lowerBound = parseLineNumberFromString(lineRangeSplit[0]);
                int upperBound = parseLineNumberFromString(lineRangeSplit[1]);
                if (lowerBound > upperBound) {
                    throw new IllegalArgumentException(
                            "Lower bound " + lineRangeSplit[0] + " cannot be higher than higher bound " +
                                    lineRangeSplit[1] + " in requested selection: " + rawLineGroups
                    );
                }
                for (int i = lowerBound ; i <= upperBound ; i ++) {
                    selectedLinesMask |= 1 << i;
                }
            }
            else if (lineRangeSplit.length == 1) {
                int lineNumber = parseLineNumberFromString(lineRange);
                selectedLinesMask |= 1 << lineNumber;
            }
            else if (lineRangeSplit.length > 2) {
                throw new IllegalArgumentException(
                        "Invalid range \"" + lineRange + "\" in requested selection: " + rawLineGroups
                );
            }
        }

        this.selectedLines = new int[Integer.bitCount(selectedLinesMask)];
        int selectedLinesPosition = 0;
        for (int i = 0 ; i <= (config.getMaxLine() - config.getMinLine()) ; i ++) {
            if ((selectedLinesMask >>> i & 1) == 0x1) {
                this.selectedLines[selectedLinesPosition++] = i;
            }
        }
    }

    private int parseLineNumberFromString(String rawLineNumber) {
        int unadjustedLineNumber;
        try {
            unadjustedLineNumber = Integer.parseInt(rawLineNumber);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot parse \"" + rawLineNumber + "\" as a line number"
            );
        }
        if (unadjustedLineNumber > config.getMaxLine() ||
                unadjustedLineNumber < config.getMinLine()) {
            throw new IllegalArgumentException(
                    "Line numbers must be between " + config.getMinLine() + " and " + config.getMaxLine() + ", but " +
                            unadjustedLineNumber + " was provided."
            );
        }
        return unadjustedLineNumber - config.getLineStartsAt();
    }
}
