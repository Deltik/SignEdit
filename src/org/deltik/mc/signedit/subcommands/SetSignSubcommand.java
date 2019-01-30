package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.committers.LineSignEditCommit;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.listeners.Interact;

import javax.inject.Inject;
import java.util.List;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

public class SetSignSubcommand implements SignSubcommand {
    private final Configuration config;
    private final Interact listener;
    private final ArgStruct argStruct;
    private final Player player;

    @Inject
    public SetSignSubcommand(Configuration config, Interact listener, ArgStruct argStruct, Player player) {
        this.config = config;
        this.listener = listener;
        this.argStruct = argStruct;
        this.player = player;
    }

    @Override
    public boolean execute() {
        int minLine = config.getMinLine();
        int maxLine = config.getMaxLine();
        int lineRelative = argStruct.getLineRelative();
        if (lineRelative > maxLine || lineRelative < minLine) {
            player.sendMessage(CHAT_PREFIX + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
            return true;
        }
        int line = lineRelative - minLine;

        String txt;
        if (argStruct.getSubcommand().equals("clear")) {
            txt = "";
        } else {
            txt = arrayToSignText(argStruct.getRemainder());
        }

        SignEditCommit commit = new LineSignEditCommit(line, minLine, txt);
        SignSubcommand.autocommit(commit, player, listener, config);
        return true;
    }

    private String arrayToSignText(List<String> textArray) {
        return String.join(" ", textArray).replace('&', '§');
    }
}
