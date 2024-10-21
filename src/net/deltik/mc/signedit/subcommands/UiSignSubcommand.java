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

import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.CraftBukkitReflector;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.SignEditInteractionModule;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class UiSignSubcommand extends SignSubcommand {
    /**
     * Bug reported for Minecraft 1.16.1: https://bugs.mojang.com/browse/MC-192263
     * Bug resolved in Minecraft 1.16.2: https://minecraft.gamepedia.com/Java_Edition_20w30a
     */
    private static final String QUIRKY_BUKKIT_SERVER_VERSION = "v1_16_R1";

    private final Configuration config;
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final CraftBukkitReflector reflector;

    @Inject
    public UiSignSubcommand(
            Player player,
            Configuration config,
            Map<String, Provider<SignEditInteraction>> interactions,
            CraftBukkitReflector reflector
    ) {
        super(player);
        this.config = config;
        this.interactions = interactions;
        this.reflector = reflector;
    }

    @Override
    public SignEditInteraction execute() {
        return interactions.get(getImplementationName(config, reflector)).get();
    }

    public static String getImplementationName(Configuration config, CraftBukkitReflector reflector) {
        String value = config.getSignUi().toLowerCase();

        if ("editablebook".equals(value)) {
            return SignEditInteractionModule.UI_EDITABLE_BOOK;
        } else if ("native".equals(value)) {
            return SignEditInteractionModule.UI_NATIVE;
        }

        // Auto mode
        if (QUIRKY_BUKKIT_SERVER_VERSION.compareTo(reflector.BUKKIT_SERVER_VERSION) == 0) {
            return SignEditInteractionModule.UI_EDITABLE_BOOK;
        }
        return SignEditInteractionModule.UI_NATIVE;
    }
}