/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
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

package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;

import javax.inject.Inject;
import java.util.Arrays;

import static org.deltik.mc.signedit.ArgParser.ALL_LINES_SELECTED;
import static org.deltik.mc.signedit.ArgParser.NO_LINES_SELECTED;

public class CopySignEditInteraction implements SignEditInteraction {
    private final ArgParser argParser;
    private final SignText signText;
    private final SignTextClipboardManager clipboardManager;
    private final ChatComms comms;

    @Inject
    public CopySignEditInteraction(
            ArgParser argParser,
            SignText signText,
            SignTextClipboardManager clipboardManager,
            ChatComms comms) {
        this.argParser = argParser;
        this.signText = signText;
        this.clipboardManager = clipboardManager;
        this.comms = comms;
    }

    @Override
    public void interact(Player player, Sign sign) {
        int[] selectedLines = argParser.getLinesSelection();
        if (Arrays.equals(selectedLines, NO_LINES_SELECTED)) {
            selectedLines = ALL_LINES_SELECTED;
        }
        for (int selectedLine : selectedLines) {
            signText.setLineLiteral(selectedLine, sign.getLine(selectedLine));
        }

        clipboardManager.setClipboard(player, signText);

        comms.tellPlayer(comms.t("lines_copied_section"));
        comms.dumpLines(signText.getLines());
    }

    @Override
    public String getName() {
        return "copy_sign_text";
    }
}
