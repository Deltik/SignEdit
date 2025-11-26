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

import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.CraftBukkitReflector;
import net.deltik.mc.signedit.interactions.InteractionFactory;
import net.deltik.mc.signedit.interactions.SignEditInteraction;

@SignSubcommandInfo(name = "ui")
public class UiSignSubcommand extends SignSubcommand {
    /**
     * Bug reported for Minecraft 1.16.1: https://bugs.mojang.com/browse/MC-192263
     * Bug resolved in Minecraft 1.16.2: https://minecraft.gamepedia.com/Java_Edition_20w30a
     */
    private static final String QUIRKY_BUKKIT_SERVER_VERSION = "v1_16_R1";

    public UiSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        Configuration config = context().services().config();
        CraftBukkitReflector reflector = context().services().reflector();
        String interactionName = getImplementationName(config, reflector);
        return context().services().interactionFactory()
                .create(interactionName, context());
    }

    public static String getImplementationName(Configuration config, CraftBukkitReflector reflector) {
        String value = config.getSignUi().toLowerCase();

        if ("editablebook".equals(value)) {
            return InteractionFactory.UI_EDITABLE_BOOK;
        } else if ("native".equals(value)) {
            return InteractionFactory.UI_NATIVE;
        }

        // Auto mode
        if (QUIRKY_BUKKIT_SERVER_VERSION.compareTo(reflector.BUKKIT_SERVER_VERSION) == 0) {
            return InteractionFactory.UI_EDITABLE_BOOK;
        }
        return InteractionFactory.UI_NATIVE;
    }
}