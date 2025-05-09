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
import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllSignSubcommand extends SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final ArgParser argParser;
    private final SignText signText;
    private final Configuration config;

    @Inject
    public AllSignSubcommand(
            Player player,
            Map<String, Provider<SignEditInteraction>> interactions,
            ArgParser argParser,
            SignText signText,
            Configuration config
    ) {
        super(player);
        this.interactions = interactions;
        this.argParser = argParser;
        this.signText = signText;
        this.config = config;
    }

    @Override
    public SignEditInteraction execute() {
        String[] lines = parseMultiLineText(String.join(" ", argParser.getRemainder()));
        
        for (int i = 0; i < lines.length && i < 4; i++) {
            signText.setLineLiteral(i, lines[i]);
        }
        
        return interactions.get("Set").get();
    }
    
    private String[] parseMultiLineText(String input) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        boolean escaped = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (escaped) {
                currentLine.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                if (config.isMultilineWhitespaceStripping()) {
                    lines.add(currentLine.toString().stripTrailing());
                } else {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder();
            } else {
                currentLine.append(c);
            }
        }
        
        if (config.isMultilineWhitespaceStripping()) {
            lines.add(currentLine.toString().stripTrailing());
        } else {
            lines.add(currentLine.toString());
        }
        
        // Ensure we have 4 lines, filling empty ones with empty strings
        while (lines.size() < 4) {
            lines.add("");
        }
        
        return lines.toArray(new String[0]);
    }
}
