package org.deltik.mc.signedit;

import org.bukkit.block.Sign;

import javax.inject.Inject;

public class SignText {
    String[] lines = new String[4];
    String[] backupLines = lines;
    Sign targetSign;

    @Inject
    public SignText() {
    }

    public Sign getTargetSign() {
        return targetSign;
    }

    public void setTargetSign(Sign targetSign) {
        this.targetSign = targetSign;
    }

    public void applySign() {
        for (int i = 0; i < lines.length; i++) {
            String line = getLine(i);
            if (line != null) {
                backupLines[i] = targetSign.getLine(i);
                targetSign.setLine(i, line);
            }
        }
        targetSign.update();
    }

    public void revertSign() {
        for (int i = 0; i < lines.length; i++) {
            String backupLine = backupLines[i];
            if (backupLine != null) {
                targetSign.setLine(i, backupLine);
            }
        }
    }

    public void importSign() {
        lines = targetSign.getLines();
    }

    public void setLineLiteral(int lineNumber, String value) {
        lines[lineNumber] = value;
    }

    public void setLine(int lineNumber, String value) {
        if (value != null) {
            value = value.replace('&', '§');
        }
        setLineLiteral(lineNumber, value);
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

    public String getLineParsed(int lineNumber) {
        String line = lines[lineNumber];
        if (line == null) return null;
        line = line.replace('§', '&');
        return line;
    }
}
