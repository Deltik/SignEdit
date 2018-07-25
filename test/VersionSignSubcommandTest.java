import org.bukkit.plugin.PluginDescriptionFile;
import org.deltik.mc.signedit.Main;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({Main.class, PluginDescriptionFile.class})
public class VersionSignSubcommandTest extends SignEditTest {
    @Test
    public void signVersionShowsVersion() {
        String expected = "1.3.1";

        // FIXME: Dependency injection of the PluginDescriptionFile so we don't have to do this disgusting reflection
        PowerMockito.mockStatic(Main.class);
        Main mainMock = mock(Main.class);
        PluginDescriptionFile pluginDescriptionFileMock = mock(PluginDescriptionFile.class);
        Whitebox.setInternalState(Main.class, "instance", mainMock);
        when(mainMock.getDescription()).thenReturn(pluginDescriptionFileMock);
        when(pluginDescriptionFileMock.getVersion()).thenReturn(expected);
        String argsString = "version";

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(contains(expected));
    }
}
