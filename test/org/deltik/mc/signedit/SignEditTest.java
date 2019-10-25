package org.deltik.mc.signedit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
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
        listener = new SignEditListener(null, null);
        doReturn(false).when(spyConfig).writeSaneConfig(new YamlConfiguration());

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
