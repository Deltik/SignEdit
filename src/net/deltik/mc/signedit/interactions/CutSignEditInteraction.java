/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.Arrays;

import static net.deltik.mc.signedit.ArgParser.ALL_LINES_SELECTED;
import static net.deltik.mc.signedit.ArgParser.NO_LINES_SELECTED;

public class CutSignEditInteraction implements SignEditInteraction {
    private final ArgParser argParser;
    private final SignText sourceSign;
    private final SignText clipboard;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public CutSignEditInteraction(
            ArgParser argParser,
            SignText signText,
            SignEditValidator validator,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            ChatComms comms) {
        this.argParser = argParser;
        this.sourceSign = signText;
        this.clipboard = new SignText(validator);
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public void interact(Player player, Sign sign) {
        int[] selectedLines = argParser.getLinesSelection();
        if (Arrays.equals(selectedLines, NO_LINES_SELECTED)) {
            selectedLines = ALL_LINES_SELECTED;
        }

        sourceSign.setTargetSign(sign);

        for (int selectedLine : selectedLines) {
            clipboard.setLineLiteral(selectedLine, sign.getLine(selectedLine));
            sourceSign.setLineLiteral(selectedLine, "");
        }

        sourceSign.applySign();
        if (sourceSign.signChanged()) {
            historyManager.getHistory(player).push(sourceSign);
        }
        clipboardManager.setClipboard(player, clipboard);

        comms.tellPlayer(comms.t("lines_cut_section"));
        comms.dumpLines(clipboard.getLines());
    }

    @Override
    public String getName() {
        return "cut_sign_text";
    }
}
