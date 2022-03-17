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

package net.deltik.mc.signedit.subcommands;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistory;
import net.deltik.mc.signedit.SignTextHistoryManager;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class RedoSignSubcommand implements SignSubcommand {
    private final Player player;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public RedoSignSubcommand(Player player, ChatComms comms, SignTextHistoryManager historyManager) {
        this.player = player;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        SignTextHistory history = historyManager.getHistory(player);
        SignText redoneSignText = history.redo();
        comms.compareSignText(redoneSignText);
        return null;
    }
}
