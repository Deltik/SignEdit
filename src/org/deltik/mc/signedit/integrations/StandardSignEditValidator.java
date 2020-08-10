/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit.integrations;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;

import javax.inject.Inject;

public class StandardSignEditValidator implements SignEditValidator {
    protected final PluginManager pluginManager;
    protected final Player player;

    @Inject
    public StandardSignEditValidator(
            Player player,
            PluginManager pluginManager
    ) {
        this.player = player;
        this.pluginManager = pluginManager;
    }

    @Override
    public void validate(Sign target, SignText signText) {
        SignChangeEvent signChangeEvent = new SignChangeEvent(
                target.getBlock(),
                player,
                target.getLines()
        );
        pluginManager.callEvent(signChangeEvent);
        if (signChangeEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
    }
}
