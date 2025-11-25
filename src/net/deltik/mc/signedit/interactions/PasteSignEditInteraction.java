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

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class PasteSignEditInteraction implements SignEditInteraction {
    private final SignTextClipboardManager clipboardManager;
    private final Supplier<SignText> signTextSupplier;
    private final SignTextHistoryManager historyManager;
    private final ChatCommsFactory chatCommsFactory;

    public PasteSignEditInteraction(
            SignTextClipboardManager clipboardManager,
            SubcommandContext context,
            SignTextHistoryManager historyManager,
            ChatCommsFactory chatCommsFactory
    ) {
        this.clipboardManager = clipboardManager;
        this.signTextSupplier = context::createSignText;
        this.historyManager = historyManager;
        this.chatCommsFactory = chatCommsFactory;
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        SignText clipboard = clipboardManager.getClipboard(player);
        SignText signText = signTextSupplier.get();
        signText.setTargetSign(sign, side);

        for (int i = 0; i < clipboard.getLines().length; i++) {
            signText.setLineLiteral(i, clipboard.getLine(i));
        }

        ChatComms comms = chatCommsFactory.create(player);

        signText.applySignAutoWax(player, comms, signText::applySign);
        if (signText.signTextChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }

    @Override
    public String getName() {
        return "paste_lines_from_clipboard";
    }
}
