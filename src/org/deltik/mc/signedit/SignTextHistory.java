package org.deltik.mc.signedit;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.LinkedList;

public class SignTextHistory {
    private Provider<SignText> signTextProvider;
    private final Configuration config;
    private LinkedList<SignText> history = new LinkedList<>();
    int tailPosition = 0;

    @Inject
    public SignTextHistory(Provider<SignText> signTextProvider, Configuration config) {
        this.signTextProvider = signTextProvider;
        this.config = config;
    }

    public void push(SignText signText) {
        while (history.size() > tailPosition) {
            history.removeLast();
        }
        history.addLast(signText);
        tailPosition = history.size();
    }

    public void undo() {
        if (tailPosition <= 0) {
            throw new IndexOutOfBoundsException("Nothing to undo");
        }
        tailPosition --;
        SignText previousSignText = history.get(tailPosition);
        previousSignText.revertSign();
    }

    public void redo() {
        if (tailPosition == history.size()) {
            throw new IndexOutOfBoundsException("Nothing to redo");
        }
        SignText nextSignText = history.get(tailPosition);
        nextSignText.applySign();
        tailPosition ++;
    }
}
