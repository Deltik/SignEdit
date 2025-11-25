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

package net.deltik.mc.signedit.subcommands;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistory;
import net.deltik.mc.signedit.interactions.SignEditInteraction;

public class RedoSignSubcommand extends SignSubcommand {
    public RedoSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        SignTextHistory history = context().services().historyManager().getHistory(player());
        ChatComms comms = context().services().chatCommsFactory().create(player());
        SignText redoneSignText = history.redo(comms);
        comms.compareSignText(redoneSignText);
        return null;
    }
}
