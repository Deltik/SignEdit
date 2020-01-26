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

package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.SignEditPlugin;
import org.junit.Test;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public class VersionSignSubcommandTest {
    @Test
    public void signVersionShowsVersion() {
        String expected = "1.3.1";

        Player player = mock(Player.class);
        Configuration config = mock(Configuration.class);
        when(config.getLocale()).thenReturn(new Locale("en"));
        ChatComms comms = new ChatComms(player, config);
        PluginDescriptionFile pluginDescriptionFile = mock(PluginDescriptionFile.class);
        SignEditPlugin plugin = mock(SignEditPlugin.class);
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);
        when(pluginDescriptionFile.getVersion()).thenReturn(expected);
        SignSubcommand subcommand = new VersionSignSubcommand(plugin, comms);
        subcommand.execute();

        verify(player).sendMessage(contains(expected));
    }
}
