package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.exceptions.MissingLineSelectionException;
import org.deltik.mc.signedit.interactions.SetSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class SetSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final ArgParser argParser;
    private final SignText signText;
    private final ChatComms comms;

    @Inject
    public SetSignSubcommand(
            Configuration config,
            ArgParser argParser,
            SignText signText,
            ChatComms comms
    ) {
        this.config = config;
        this.argParser = argParser;
        this.signText = signText;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        LineSelectionException selectedLinesError = argParser.getSelectedLinesError();
        if (selectedLinesError != null) {
            throw selectedLinesError;
        }
        int[] selectedLines = argParser.getSelectedLines();
        if (selectedLines.length <= 0) {
            throw new MissingLineSelectionException();
        }

        String text;
        if (argParser.getSubcommand().equals("clear")) {
            text = "";
        } else {
            text = String.join(" ", argParser.getRemainder());
        }

        for (int selectedLine : selectedLines) {
            signText.setLine(selectedLine, text);
        }

        return new SetSignEditInteraction(signText, comms);
    }
}
