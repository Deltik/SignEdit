package org.deltik.mc.signedit.exceptions;

public class RangeParseLineSelectionException extends LineSelectionException {
    private final String wholeSelection;
    private final String badRange;

    public RangeParseLineSelectionException(String wholeSelection, String badRange) {
        this.wholeSelection = wholeSelection;
        this.badRange = badRange;
    }

    @Override
    public String getMessage() {
        return this.wholeSelection;
    }

    public String getBadRange() {
        return badRange;
    }
}
