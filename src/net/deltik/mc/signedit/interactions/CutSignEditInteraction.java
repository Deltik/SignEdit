/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.shims.ISignSide;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static net.deltik.mc.signedit.LineSelectorParser.ALL_LINES_SELECTED;
import static net.deltik.mc.signedit.LineSelectorParser.NO_LINES_SELECTED;

public class CutSignEditInteraction extends SignEditInteraction {
    private final SignText clipboard;

    public CutSignEditInteraction(SubcommandContext context) {
        super(context);
        this.clipboard = context.createSignText();
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        int[] selectedLines = argParser().getLinesSelection();
        if (Arrays.equals(selectedLines, NO_LINES_SELECTED)) {
            selectedLines = ALL_LINES_SELECTED;
        }

        signText().setTargetSign(sign, side);

        ISignSide signSide = sign.getSide(side);
        for (int selectedLine : selectedLines) {
            clipboard.setLineLiteral(selectedLine, signSide.getLine(selectedLine));
            signText().setLineLiteral(selectedLine, "");
        }

        ChatComms comms = chatCommsFactory().create(player);

        signText().applySignAutoWax(player, comms, signText()::applySign);
        if (signText().signTextChanged()) {
            historyManager().getHistory(player).push(signText());
        }
        clipboardManager().setClipboard(player, clipboard);

        comms.tell(comms.t("lines_cut_section"));
        comms.dumpLines(clipboard.getLines());
    }

    @Override
    public String getName() {
        return "cut_sign_text";
    }
}