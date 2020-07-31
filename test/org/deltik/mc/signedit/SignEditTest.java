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

package org.deltik.mc.signedit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.listeners.SignEditListener;
import org.deltik.mc.signedit.subcommands.UiSignSubcommand;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.mockito.Mockito.validateMockitoUsage;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, PlayerInteractEvent.class, SignCommand.class})
public abstract class SignEditTest {
    SignCommand signCommand;
    Player player;
    Command command;
    Sign sign;
    Block block;
    Configuration spyConfig;
    SignEditListener listener;
    String cString = "signedit";
    UiSignSubcommand uiSignSubcommand;
    Server server;
    PluginManager pluginManager;

    @Before
    public void setUp() throws Exception {
        Configuration config = new Configuration(File.createTempFile("SignEdit-", "-config.yml"));
        spyConfig = spy(config);
        listener = new SignEditListener(null, null, null);
        doReturn(false).when(spyConfig).writeSaneConfig();

        uiSignSubcommand = mock(UiSignSubcommand.class);
        whenNew(UiSignSubcommand.class).withAnyArguments().thenReturn(uiSignSubcommand);

        player = mock(Player.class);
        command = mock(Command.class);
        sign = mock(Sign.class);
        block = mock(Block.class);
        when(player.hasPermission("SignEdit.use")).thenReturn(true);
        when(block.getState()).thenReturn(sign);

        server = mock(Server.class);
        pluginManager = mock(PluginManager.class);
        mockStatic(Bukkit.class);
        when(Bukkit.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }
}
