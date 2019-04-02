package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.interactions.SetSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;
import java.util.List;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class SetSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final SignEditListener listener;
    private final ArgParser argParser;
    private final Player player;
    private final SignText signText;

    @Inject
    public SetSignSubcommand(
            Configuration config,
            SignEditListener listener,
            ArgParser argParser,
            Player player,
            SignText signText
    ) {
        this.config = config;
        this.listener = listener;
        this.argParser = argParser;
        this.player = player;
        this.signText = signText;
    }

    @Override
    public boolean execute() {
        if (SignSubcommand.reportLineSelectionError(argParser.getSelectedLinesError(), player)) {
            return true;
        }
        int[] selectedLines = argParser.getSelectedLines();
        if (selectedLines.length <= 0) {
            player.sendMessage(CHAT_PREFIX + "§c" + "A line selection is required but was not provided.");
            return true;
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

        SignEditInteraction interaction = new SetSignEditInteraction(signText, config.getLineStartsAt());
        SignSubcommand.autointeract(interaction, player, listener, config);
        return true;
    }
}
