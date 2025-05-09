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
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.exceptions.NotAFloorSignException;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.RotateSignEditInteraction;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class RotateSignSubcommand extends SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final ArgParser argParser;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;

    @Inject
    public RotateSignSubcommand(
            Player player,
            Map<String, Provider<SignEditInteraction>> interactions,
            ArgParser argParser,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder
    ) {
        super(player);
        this.interactions = interactions;
        this.argParser = argParser;
        this.commsBuilder = commsBuilder;
    }

    @Override
    public SignEditInteraction execute() {
        SignEditInteraction interaction = interactions.get("Rotate").get();
        
        // Parse rotation
        if (!argParser.getRemainder().isEmpty()) {
            String rotationArg = argParser.getRemainder().get(0).toLowerCase();
            
            try {
                // Check if it's the lock mode
                if ("lock".equals(rotationArg)) {
                    ((RotateSignEditInteraction) interaction).setLocked(true);
                    return interaction;
                }
                
                // Check if it's a direction string
                RotateSignEditInteraction.Direction direction = parseDirection(rotationArg);
                if (direction != null) {
                    ((RotateSignEditInteraction) interaction).setTargetDirection(direction);
                    return interaction;
                }
                
                // Check if it's a rotation offset or absolute rotation
                int rotation = -1;
                if (rotationArg.startsWith("+")) {
                    // Positive relative rotation
                    rotation = Integer.parseInt(rotationArg.substring(1));
                    ((RotateSignEditInteraction) interaction).setRelativeRotation(rotation);
                } else if (rotationArg.startsWith("-")) {
                    // Negative relative rotation
                    rotation = Integer.parseInt(rotationArg);
                    ((RotateSignEditInteraction) interaction).setRelativeRotation(rotation);
                } else {
                    // Absolute rotation
                    rotation = Integer.parseInt(rotationArg);
                    ((RotateSignEditInteraction) interaction).setAbsoluteRotation(rotation);
                }
            } catch (NumberFormatException e) {
                // Invalid rotation, use default of interactive mode
                ChatComms comms = commsBuilder.commandSender(getPlayer()).build().comms();
                comms.tell(comms.t("invalid_rotation_format"));
            }
        }
        
        return interaction;
    }
    
    private RotateSignEditInteraction.Direction parseDirection(String directionStr) {
        // Clean up the direction string
        directionStr = directionStr.replace("_", "").toLowerCase();
        
        // Try to match with known directions
        switch (directionStr) {
            case "north":
                return RotateSignEditInteraction.Direction.NORTH;
            case "northeast":
                return RotateSignEditInteraction.Direction.NORTH_EAST;
            case "east":
                return RotateSignEditInteraction.Direction.EAST;
            case "southeast":
                return RotateSignEditInteraction.Direction.SOUTH_EAST;
            case "south":
                return RotateSignEditInteraction.Direction.SOUTH;
            case "southwest":
                return RotateSignEditInteraction.Direction.SOUTH_WEST;
            case "west":
                return RotateSignEditInteraction.Direction.WEST;
            case "northwest":
                return RotateSignEditInteraction.Direction.NORTH_WEST;
            default:
                return null;
        }
    }
}
