package org.deltik.mc.signedit.exceptions;

public class RangeOrderLineSelectionException extends LineSelectionException {
    private final String wholeSelection;
    private final String invalidLowerBound;
    private final String invalidUpperBound;

    public RangeOrderLineSelectionException(String wholeSelection, String invalidLowerBound, String invalidUpperBound) {
        this.wholeSelection = wholeSelection;
        this.invalidLowerBound = invalidLowerBound;
        this.invalidUpperBound = invalidUpperBound;
    }

    @Override
    public String getMessage() {
        return this.wholeSelection;
    }

    public String getInvalidLowerBound() {
        return invalidLowerBound;
    }

    public String getInvalidUpperBound() {
        return invalidUpperBound;
    }
}
