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
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import org.bukkit.entity.Player;

public class PasteSignEditInteraction extends SignEditInteraction {
    public PasteSignEditInteraction(SubcommandContext context) {
        super(context);
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        SignText clipboard = clipboardManager().getClipboard(player);
        SignText signText = context().createSignText();
        signText.setTargetSign(sign, side);

        for (int i = 0; i < clipboard.getLines().length; i++) {
            signText.setLineLiteral(i, clipboard.getLine(i));
        }

        ChatComms comms = chatCommsFactory().create(player);

        signText.applySignAutoWax(player, comms, signText::applySign);
        if (signText.signTextChanged()) {
            historyManager().getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }

    @Override
    public String getName() {
        return "paste_lines_from_clipboard";
    }
}
