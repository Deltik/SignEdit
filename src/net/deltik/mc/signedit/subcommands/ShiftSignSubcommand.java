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

import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistoryManager;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class ShiftSignSubcommand extends SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final ArgParser argParser;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;

    @Inject
    public ShiftSignSubcommand(
            Player player,
            Map<String, Provider<SignEditInteraction>> interactions,
            ArgParser argParser,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
        super(player);
        this.interactions = interactions;
        this.argParser = argParser;
        this.signText = signText;
        this.historyManager = historyManager;
    }

    @Override
    public SignEditInteraction execute() {
        int offset = 0;
        
        if (!argParser.getRemainder().isEmpty()) {
            String offsetArg = argParser.getRemainder().get(0);
            
            try {
                if (offsetArg.startsWith("+")) {
                    offset = Integer.parseInt(offsetArg.substring(1));
                } else if (offsetArg.startsWith("-")) {
                    offset = Integer.parseInt(offsetArg);
                } else {
                    offset = Integer.parseInt(offsetArg);
                }
                
                // Normalize offset to be between -3 and 3
                offset = ((offset % 4) + 4) % 4;
                if (offset == 0) {
                    // No shifting needed if offset is 0
                    return interactions.get("Set").get();
                }
            } catch (NumberFormatException e) {
                // Invalid offset, use default of 0
            }
        }
        
        // Create a temporary array to hold the shifted lines
        String[] originalLines = new String[4];
        String[] shiftedLines = new String[4];
        
        // Copy original lines
        for (int i = 0; i < 4; i++) {
            originalLines[i] = signText.getLineParsed(i);
        }
        
        // Shift the lines
        for (int i = 0; i < 4; i++) {
            int newIndex = (i + offset) % 4;
            shiftedLines[newIndex] = originalLines[i];
        }
        
        // Apply the shifted lines back to the sign
        for (int i = 0; i < 4; i++) {
            signText.setLineLiteral(i, shiftedLines[i]);
        }
        
        return interactions.get("Set").get();
    }
}
