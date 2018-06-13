package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.committers.LineSignEditCommit;
import org.deltik.mc.signedit.committers.SignEditCommit;

import java.util.List;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class SetSignSubcommand extends SignSubcommand {
    @Override
    public boolean execute() {
        int minLine = config.getMinLine();
        int maxLine = config.getMaxLine();
        if (argStruct.lineRelative > maxLine || argStruct.lineRelative < minLine) {
            player.sendMessage(CHAT_PREFIX + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
            return true;
        }
        int line = argStruct.lineRelative - minLine;

        String txt;
        if (argStruct.subcommand.equals("clear")) {
            txt = "";
        } else {
            txt = arrayToSignText(argStruct.remainder);
        }

        SignEditCommit commit = new LineSignEditCommit(line, minLine, txt);
        return autocommit(commit);
    }

    private String arrayToSignText(List<String> textArray) {
        return String.join(" ", textArray).replace('&', '§');
    }
}
