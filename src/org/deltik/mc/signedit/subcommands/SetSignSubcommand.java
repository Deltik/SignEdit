package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.interactions.SetSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class SetSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final ArgParser argParser;
    private final Player player;
    private final SignText signText;

    @Inject
    public SetSignSubcommand(
            Configuration config,
            ArgParser argParser,
            Player player,
            SignText signText
    ) {
        this.config = config;
        this.argParser = argParser;
        this.player = player;
        this.signText = signText;
    }

    @Override
    public SignEditInteraction execute() {
        Exception selectedLinesError = argParser.getSelectedLinesError();
        if (selectedLinesError instanceof LineSelectionException) {
            player.sendMessage(CHAT_PREFIX + "§c" + selectedLinesError.getMessage());
            return null;
        }
        int[] selectedLines = argParser.getSelectedLines();
        if (selectedLines.length <= 0) {
            player.sendMessage(CHAT_PREFIX + "§c" + "A line selection is required but was not provided.");
            return null;
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

        return new SetSignEditInteraction(signText, config.getLineStartsAt());
    }
}
