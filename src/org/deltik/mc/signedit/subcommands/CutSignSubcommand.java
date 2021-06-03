/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit.subcommands;

import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class CutSignSubcommand implements SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;

    @Inject
    public CutSignSubcommand(
            Map<String, Provider<SignEditInteraction>> interactions
    ) {
        this.interactions = interactions;
    }

    @Override
    public SignEditInteraction execute() {
        return interactions.get("Cut").get();
    }
}
