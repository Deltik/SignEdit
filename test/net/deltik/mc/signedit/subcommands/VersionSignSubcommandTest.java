/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.SignEditPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

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
        Plugin plugin = mock(SignEditPlugin.class);
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);
        when(pluginDescriptionFile.getVersion()).thenReturn(expected);
        SignSubcommand subcommand = new VersionSignSubcommand(plugin, comms);
        subcommand.execute();

        verify(player).sendMessage(contains(expected));
    }
}
