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

import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.shims.SignHelpers;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class WaxSignSubcommand extends SignSubcommand {
    private final Map<String, Provider<SignEditInteraction>> interactions;
    private final SignText signText;

    @Inject
    public WaxSignSubcommand(
            Map<String, Provider<SignEditInteraction>> interactions,
            Player player,
            SignText signText
    ) {
        super(player);
        this.interactions = interactions;
        this.signText = signText;
    }

    @Override
    public SignEditInteraction execute() {
        signText.setShouldBeEditable(false);
        return interactions.get("Wax").get();
    }

    @Override
    public boolean isPermitted() {
        if (!SignHelpers.hasWaxableFeature()) return false;
        return super.isPermitted();
    }
}
