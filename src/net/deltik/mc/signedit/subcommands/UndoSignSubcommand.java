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

package net.deltik.mc.signedit.subcommands;

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class UndoSignSubcommand extends SignSubcommand {
    private final Player player;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignTextHistoryManager historyManager;

    @Inject
    public UndoSignSubcommand(
            Player player,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignTextHistoryManager historyManager
    ) {
        super(player);
        this.player = player;
        this.commsBuilder = commsBuilder;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        SignTextHistory history = historyManager.getHistory(player);
        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        SignText undoneSignText = history.undo(comms);
        comms.compareSignText(undoneSignText);
        return null;
    }
}
