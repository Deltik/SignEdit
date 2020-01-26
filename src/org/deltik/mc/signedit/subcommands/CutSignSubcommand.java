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

import org.deltik.mc.signedit.*;
import org.deltik.mc.signedit.interactions.CutSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;

public class CutSignSubcommand implements SignSubcommand {
    private final ArgParser argParser;
    private final Provider<SignText> signTextProvider;
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final ChatComms comms;

    @Inject
    public CutSignSubcommand(
            ArgParser argParser,
            Provider<SignText> signTextProvider,
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            ChatComms comms
    ) {
        this.argParser = argParser;
        this.signTextProvider = signTextProvider;
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        return new CutSignEditInteraction(argParser, signTextProvider, clipboardManager, historyManager, comms);
    }
}
