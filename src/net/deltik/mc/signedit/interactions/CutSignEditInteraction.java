/*
 * Copyright (C) 2017-2024 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.shims.ISignSide;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.Arrays;

import static net.deltik.mc.signedit.LineSelectorParser.ALL_LINES_SELECTED;
import static net.deltik.mc.signedit.LineSelectorParser.NO_LINES_SELECTED;

public class CutSignEditInteraction implements SignEditInteraction {
    private final ArgParser argParser;
    private final SignText sourceSign;
    private final SignText clipboard;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;

    @Inject
    public CutSignEditInteraction(
            ArgParser argParser,
            SignText signText,
            SignEditValidator validator,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder
            ) {
        this.argParser = argParser;
        this.sourceSign = signText;
        this.clipboard = new SignText(validator);
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.commsBuilder = commsBuilder;
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        int[] selectedLines = argParser.getLinesSelection();
        if (Arrays.equals(selectedLines, NO_LINES_SELECTED)) {
            selectedLines = ALL_LINES_SELECTED;
        }

        sourceSign.setTargetSign(sign, side);

        // Check if a destination line or offset is provided
        int shiftOffset = 0;
        if (!argParser.getRemainder().isEmpty()) {
            String shiftArg = argParser.getRemainder().get(0);
            try {
                if (shiftArg.startsWith("+")) {
                    shiftOffset = Integer.parseInt(shiftArg.substring(1));
                } else if (shiftArg.startsWith("-")) {
                    shiftOffset = Integer.parseInt(shiftArg);
                } else {
                    // If it's just a number, consider it a target line and calculate the offset
                    int targetLine = Integer.parseInt(shiftArg) - argParser.getConfig().getLineStartsAt();
                    shiftOffset = targetLine - selectedLines[0];
                }
            } catch (NumberFormatException ignored) {
                // Invalid shift argument, no shifting will be applied
            }
        }

        ISignSide signSide = sign.getSide(side);
        
        // Create a temporary array of line content
        String[] tempLines = new String[4];
        for (int i = 0; i < 4; i++) {
            tempLines[i] = null;
        }
        
        // Copy selected lines and clear from source
        for (int selectedLine : selectedLines) {
            tempLines[selectedLine] = signSide.getLine(selectedLine);
            sourceSign.setLineLiteral(selectedLine, "");
        }
        
        // Apply the shift if needed (handle wrapping)
        if (shiftOffset != 0) {
            String[] shiftedLines = new String[4];
            for (int i = 0; i < 4; i++) {
                shiftedLines[i] = null;
            }
            
            for (int i = 0; i < 4; i++) {
                if (tempLines[i] != null) {
                    int targetIndex = (i + shiftOffset) % 4;
                    if (targetIndex < 0) targetIndex += 4;
                    shiftedLines[targetIndex] = tempLines[i];
                }
            }
            
            // Transfer shifted lines to clipboard
            for (int i = 0; i < 4; i++) {
                if (shiftedLines[i] != null) {
                    clipboard.setLineLiteral(i, shiftedLines[i]);
                } else {
                    clipboard.setLineLiteral(i, "");
                }
            }
        } else {
            // No shifting, just use the original selection
            for (int selectedLine : selectedLines) {
                clipboard.setLineLiteral(selectedLine, tempLines[selectedLine]);
            }
        }

        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        sourceSign.applySignAutoWax(player, comms, sourceSign::applySign);
        if (sourceSign.signTextChanged()) {
            historyManager.getHistory(player).push(sourceSign);
        }
        clipboardManager.setClipboard(player, clipboard);

        comms.tell(comms.t("lines_cut_section"));
        comms.dumpLines(clipboard.getLines());
    }

    @Override
    public String getName() {
        return "cut_sign_text";
    }
}
