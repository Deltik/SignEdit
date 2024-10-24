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
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;

public class PasteSignEditInteraction implements SignEditInteraction {
    private final SignTextClipboardManager clipboardManager;
    private final Provider<SignText> signTextProvider;
    private final SignTextHistoryManager historyManager;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;

    @Inject
    public PasteSignEditInteraction(
            SignTextClipboardManager clipboardManager,
            Provider<SignText> signTextProvider,
            SignTextHistoryManager historyManager,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder
    ) {
        this.clipboardManager = clipboardManager;
        this.signTextProvider = signTextProvider;
        this.historyManager = historyManager;
        this.commsBuilder = commsBuilder;
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        SignText clipboard = clipboardManager.getClipboard(player);
        SignText signText = signTextProvider.get();
        signText.setTargetSign(sign, side);

        for (int i = 0; i < clipboard.getLines().length; i++) {
            signText.setLineLiteral(i, clipboard.getLine(i));
        }

        ChatComms comms = commsBuilder.commandSender(player).build().comms();

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
