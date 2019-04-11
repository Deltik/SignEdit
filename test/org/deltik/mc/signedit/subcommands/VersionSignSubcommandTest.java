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
