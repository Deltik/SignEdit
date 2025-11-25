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

import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.exceptions.MissingLineSelectionException;
import net.deltik.mc.signedit.interactions.InteractionFactory;
import net.deltik.mc.signedit.interactions.SignEditInteraction;

@SignSubcommandInfo(name = "set", supportsLineSelector = true)
public class SetSignSubcommand extends SignSubcommand {
    public SetSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        int[] selectedLines = argParser().getLinesSelection();
        if (selectedLines.length <= 0) {
            throw new MissingLineSelectionException();
        }

        String text = String.join(" ", argParser().getRemainder());
        SignText signText = context().signText();

        for (int selectedLine : selectedLines) {
            signText.setLine(selectedLine, text);
        }

        return context().services().interactionFactory()
                .create(InteractionFactory.INTERACTION_SET, context());
    }
}
