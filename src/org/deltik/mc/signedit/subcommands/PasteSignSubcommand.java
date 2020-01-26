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

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.NullClipboardException;
import org.deltik.mc.signedit.interactions.PasteSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;

public class PasteSignSubcommand implements SignSubcommand {
    private final Player player;
    private final SignTextClipboardManager clipboardManager;
    private final Provider<SignText> signTextProvider;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public PasteSignSubcommand(
            Player player,
            SignTextClipboardManager clipboardManager,
            Provider<SignText> signTextProvider,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.player = player;
        this.clipboardManager = clipboardManager;
        this.signTextProvider = signTextProvider;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        if (clipboardManager.getClipboard(player) == null) {
            throw new NullClipboardException();
        }

        return new PasteSignEditInteraction(clipboardManager, signTextProvider, historyManager, comms);
    }
}
