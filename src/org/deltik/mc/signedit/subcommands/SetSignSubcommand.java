package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.committers.LineSignEditCommit;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.listeners.Interact;

import javax.inject.Inject;
import java.util.List;

public class SetSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final Interact listener;
    private final ArgParser argParser;
    private final Player player;

    @Inject
    public SetSignSubcommand(Configuration config, Interact listener, ArgParser argParser, Player player) {
        this.config = config;
        this.listener = listener;
        this.argParser = argParser;
        this.player = player;
    }

    @Override
    public boolean execute() {
        int minLine = config.getMinLine();
        if (SignSubcommand.reportLineSelectionError(argParser.getSelectedLinesError(), player)) {
            return true;
        }
        int[] selectedLines = argParser.getSelectedLines();

        String txt;
        if (argParser.getSubcommand().equals("clear")) {
            txt = "";
        } else {
            txt = arrayToSignText(argParser.getRemainder());
        }

        SignEditCommit commit = new LineSignEditCommit(selectedLines[0], minLine, txt);
        SignSubcommand.autocommit(commit, player, listener, config);
        return true;
    }

    private String arrayToSignText(List<String> textArray) {
        return String.join(" ", textArray).replace('&', '§');
    }
}
