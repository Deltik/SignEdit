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

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

public class VersionSignSubcommand extends SignSubcommand {
    private final Plugin plugin;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final Player player;

    @Inject
    public VersionSignSubcommand(
            Plugin plugin,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            Player player
    ) {
        super(player);
        this.plugin = plugin;
        this.commsBuilder = commsBuilder;
        this.player = player;
    }

    @Override
    public SignEditInteraction execute() {
        String version = plugin.getDescription().getVersion();
        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        comms.tell(comms.t("version", version));
        return null;
    }
}
