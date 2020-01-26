/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.exceptions.MissingLineSelectionException;
import org.deltik.mc.signedit.interactions.SetSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class SetSignSubcommand implements SignSubcommand {
    private final ArgParser argParser;
    private final SignText signText;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public SetSignSubcommand(
            ArgParser argParser,
            SignText signText,
            ChatComms comms,
            SignTextHistoryManager historyManager
    ) {
        this.argParser = argParser;
        this.signText = signText;
        this.comms = comms;
        this.historyManager = historyManager;
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

        return new SetSignEditInteraction(signText, comms, historyManager);
    }
}
