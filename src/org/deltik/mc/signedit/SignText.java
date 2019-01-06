package org.deltik.mc.signedit;

public class SignText {
    private String[] lines = new String[4];

    public void setLine(int lineNumber, String value) {
        lines[lineNumber] = value;
    }

    public void clearLine(int lineNumber) {
        lines[lineNumber] = null;
    }

    public boolean lineIsSet(int lineNumber) {
        return lines[lineNumber] != null;
    }

    public String getLine(int lineNumber) {
        return lines[lineNumber];
    }
}
