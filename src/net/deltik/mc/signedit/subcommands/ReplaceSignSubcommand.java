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
import net.deltik.mc.signedit.exceptions.EmptyMainHandException;
import net.deltik.mc.signedit.exceptions.NoSignInMainHandException;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;

public class ReplaceSignSubcommand extends SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final Player player;
    private final ArgParser argParser;
    private final Configuration config;

    @Inject
    public ReplaceSignSubcommand(
            Map<String, Provider<SignEditInteraction>> interactions,
            Player player,
            ArgParser argParser,
            Configuration config
    ) {
        super(player);
        this.interactions = interactions;
        this.player = player;
        this.argParser = argParser;
        this.config = config;
    }

    @Override
    public SignEditInteraction execute() {
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        
        // Check if the player has an item in hand
        if (mainHandItem == null || mainHandItem.getType() == Material.AIR) {
            throw new EmptyMainHandException();
        }
        
        // Check if the player has a sign in their main hand
        Material material = mainHandItem.getType();
        if (!material.toString().endsWith("_SIGN")) {
            throw new NoSignInMainHandException();
        }
        
        List<String> remainder = argParser.getRemainder();
        boolean lockMode = false;
        
        if (remainder.size() > 0 && "lock".equalsIgnoreCase(remainder.get(0))) {
            lockMode = true;
            // Force clicking mode for lock
            config.setClicking("true");
        }

        SignEditInteraction replaceInteraction = interactions.get("Replace").get();
        
        if (lockMode) {
            // Set locked state
            replaceInteraction.setLocked(true);
        }
        
        return replaceInteraction;
    }
}
