package org.deltik.mc.signedit;

import org.deltik.mc.signedit.exceptions.SignTextHistoryStackBoundsException;

import javax.inject.Inject;
import java.util.LinkedList;

public class SignTextHistory {
    private final Configuration config;
    private LinkedList<SignText> history = new LinkedList<>();
    int tailPosition = 0;

    @Inject
    public SignTextHistory(Configuration config) {
        this.config = config;
    }

    public void push(SignText signText) {
        while (history.size() > tailPosition) {
            history.removeLast();
        }
        history.addLast(signText);
        tailPosition = history.size();
    }

    public int undosRemaining() {
        return tailPosition;
    }

    public int redosRemaining() {
        return history.size() - tailPosition;
    }

    public SignText undo() {
        if (tailPosition <= 0) {
            throw new SignTextHistoryStackBoundsException("nothing_to_undo");
        }
        SignText previousSignText = history.get(tailPosition - 1);
        previousSignText.revertSign();
        tailPosition--;
        return previousSignText;
    }

    public SignText redo() {
        if (tailPosition == history.size()) {
            throw new SignTextHistoryStackBoundsException("nothing_to_redo");
        }
        SignText nextSignText = history.get(tailPosition);
        nextSignText.applySign();
        tailPosition++;
        return nextSignText;
    }
}
