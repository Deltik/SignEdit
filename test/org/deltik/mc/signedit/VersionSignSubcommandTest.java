package org.deltik.mc.signedit;

import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({SignEditPlugin.class, PluginDescriptionFile.class})
public class VersionSignSubcommandTest extends SignEditTest {
    @Test
    public void signVersionShowsVersion() {
        String expected = "1.3.1";

        // FIXME: Dependency injection of the PluginDescriptionFile so we don't have to do this disgusting reflection
        PowerMockito.mockStatic(SignEditPlugin.class);
        SignEditPlugin signEditPluginMock = mock(SignEditPlugin.class);
        PluginDescriptionFile pluginDescriptionFileMock = mock(PluginDescriptionFile.class);
        Whitebox.setInternalState(SignEditPlugin.class, "instance", signEditPluginMock);
        when(signEditPluginMock.getDescription()).thenReturn(pluginDescriptionFileMock);
        when(pluginDescriptionFileMock.getVersion()).thenReturn(expected);
        String argsString = "version";

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(contains(expected));
    }
}
