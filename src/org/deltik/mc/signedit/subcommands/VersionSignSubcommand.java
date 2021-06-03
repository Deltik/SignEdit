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

import org.bukkit.plugin.Plugin;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;

public class VersionSignSubcommand implements SignSubcommand {
    private final Plugin plugin;
    private final ChatComms comms;

    @Inject
    public VersionSignSubcommand(Plugin plugin, ChatComms comms) {
        this.plugin = plugin;
        this.comms = comms;
    }

    @Override
    public SignEditInteraction execute() {
        String version = plugin.getDescription().getVersion();
        comms.tellPlayer(comms.t("version", version));
        return null;
    }
}
