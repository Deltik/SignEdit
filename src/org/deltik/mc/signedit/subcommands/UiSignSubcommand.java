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

import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class UiSignSubcommand implements SignSubcommand {
    private final SignEditListener listener;
    private final SignText signText;
    private final MinecraftReflector reflector;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public UiSignSubcommand(
            SignEditListener listener,
            SignText signText,
            MinecraftReflector reflector,
            ChatComms comms,
            SignTextHistoryManager historyManager
    ) {
        this.listener = listener;
        this.signText = signText;
        this.reflector = reflector;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        return new UiSignEditInteraction(reflector, listener, comms, signText, historyManager);
    }
}