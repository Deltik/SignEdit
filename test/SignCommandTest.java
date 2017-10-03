import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.SignEdit.Commands.SignCommand;
import org.deltik.mc.SignEdit.Configuration;
import org.deltik.mc.SignEdit.EventHandler.Interact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ JavaPlugin.class, PlayerInteractEvent.class })
public class SignCommandTest {
    private SignCommand signCommand;
    private Player player;
    private Command command;
    private Sign sign;
    private Block block;
    private Configuration spyConfig;

    @Before
    public void setUp() throws IOException {
        Configuration config = new Configuration(File.createTempFile("SignEdit-", "-config.yml"));
        spyConfig = spy(config);
        doReturn(false).when(spyConfig).writeFullConfig(new YamlConfiguration());
        signCommand = new SignCommand(spyConfig);

        player = mock(Player.class);
        command = mock(Command.class);
        sign = mock(Sign.class);
        block = mock(Block.class);
        when(player.hasPermission("SignEdit.use")).thenReturn(true);
        when(block.getState()).thenReturn(sign);
    }

    @Test
    public void signLineShouldUpdateWhenLookingAtSignAndAllowedToEditBySight() {
        String cString = "sign";
        String argsString = "set 1 alpha bravo charlie";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "alpha bravo charlie");
    }

    @Test
    public void signLineShouldUpdateShorthand() {
        String cString = "sign";
        String argsString = "2 delta echo foxtrot";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(1, "delta echo foxtrot");
    }

    @Test
    public void signLineShouldUpdateWhenRightClickingSignAndAllowedToEditByRightClick() {
        String cString = "sign";
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        doReturn(true).when(spyConfig).allowedToEditSignByRightClick();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(Interact.pendingSignEdits.containsKey(player));

        PlayerInteractEvent interactEvent = spy(mock(PlayerInteractEvent.class));

        when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(interactEvent.getClickedBlock()).thenReturn(block);
        when(interactEvent.getPlayer()).thenReturn(player);

        Interact interact = new Interact();
        interact.onInt(interactEvent);

        verify(sign).setLine(2, "xray yankee zulu");
    }
}
