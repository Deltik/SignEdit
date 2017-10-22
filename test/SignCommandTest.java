import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.subcommands.UiSignSubcommand;
import org.deltik.mc.signedit.listeners.Interact;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PlayerInteractEvent.class, SignCommand.class})
public class SignCommandTest {
    private SignCommand signCommand;
    private Player player;
    private Command command;
    private Sign sign;
    private Block block;
    private Configuration spyConfig;
    private Interact listener;
    private String cString = "signedit";
    private UiSignSubcommand uiSignSubcommand;

    @Before
    public void setUp() throws Exception {
        Configuration config = new Configuration(File.createTempFile("SignEdit-", "-config.yml"));
        spyConfig = spy(config);
        listener = new Interact(spyConfig);
        doReturn(false).when(spyConfig).writeFullConfig(new YamlConfiguration());

        uiSignSubcommand = mock(UiSignSubcommand.class);
        whenNew(UiSignSubcommand.class).withAnyArguments().thenReturn(uiSignSubcommand);

        signCommand = new SignCommand(spyConfig, listener);

        player = mock(Player.class);
        command = mock(Command.class);
        sign = mock(Sign.class);
        block = mock(Block.class);
        when(player.hasPermission("SignEdit.use")).thenReturn(true);
        when(block.getState()).thenReturn(sign);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void signLineShouldUpdateWhenLookingAtSignAndAllowedToEditBySight() {
        String argsString = "set 1 alpha bravo charlie";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "alpha bravo charlie");
    }

    @Test
    public void signLineShouldUpdateShorthand() {
        String argsString = "2 delta echo foxtrot";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(1, "delta echo foxtrot");
    }

    @Test
    public void signLineShouldBeBlankedByBlankSet() {
        String argsString = "set 1";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "");
    }

    @Test
    public void signLineShouldBeBlankedByClear() {
        String argsString = "clear 1 and ignore additional arguments";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "");
    }

    @Test
    public void sayLineBlanked() {
        String argsString = "set 2";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(matches("(?i)^.*line.*2.*blank.*$"));
    }

    @Test
    public void sayLineUnchanged() {
        String argsString = "set 2 no difference";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();
        when(sign.getLine(1)).thenReturn("no difference");

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(matches("(?i)^.*line.*2.*unchang.*$"));
    }

    @Test
    public void sayLineChanged() {
        String argsString = "set 2 yes difference";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();
        when(sign.getLine(1)).thenReturn("no difference");

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(matches("(?i)^.*line.*2.*(?<!un)chang.*$"));
    }

    @Test
    public void sayUsageWhenInvalidArgumentsProvided() {
        String argsString = "anything 1 that 2 doesn't 3 fit";

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player, atLeastOnce()).sendMessage(matches("^.*Usage.*$"));
    }

    @Test
    public void sayUsageWhenNoArgumentsProvided() {
        signCommand.onCommand(player, command, cString, new String[0]);

        verify(player, atLeastOnce()).sendMessage(matches("^.*Usage.*$"));
    }

    @Test
    public void sayUsageWhenInsufficientArgumentsProvided() {
        signCommand.onCommand(player, command, cString, new String[]{"set"});

        verify(player, atLeastOnce()).sendMessage(matches("^.*Usage.*$"));
    }

    @Test
    public void sayLineNumberRangeWhenInvalidLineNumberProvided() {
        String argsString = "set -3";

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*line.*from.*to.*$"));
    }

    @Test
    public void sayLineNumberRangeWhenNotANumberProvided() {
        String argsString = "set not-a-number text that should never be set";

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*line.*from.*to.*$"));
    }

    @Test
    public void sayLineNumberRangeWhenOutOfBoundsLineStartsAt1() {
        String argsString = "set 0 fail";

        doReturn(1).when(spyConfig).getLineStartsAt();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*line.*from.*to.*$"));
    }

    @Test
    public void signLineShouldUpdateWhenInBoundsLineStartsAt1() {
        String argsString = "set 4 pass";

        doReturn(1).when(spyConfig).getLineStartsAt();
        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(3, "pass");
    }

    @Test
    public void sayLineNumberRangeWhenOutOfBoundsLineStartsAt0() {
        String argsString = "set 4 fail";

        doReturn(0).when(spyConfig).getLineStartsAt();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*line.*$"));
    }

    @Test
    public void signLineShouldUpdateWhenInBoundsLineStartsAt0() {
        String argsString = "set 0 pass";

        doReturn(0).when(spyConfig).getLineStartsAt();
        when(player.getTargetBlock(null, 10)).thenReturn(block);
        doReturn(true).when(spyConfig).allowedToEditSignBySight();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "pass");
    }

    @Test
    public void signLineShouldUpdateWhenRightClickingSignAndAllowedToEditByRightClick() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        doReturn(true).when(spyConfig).allowedToEditSignByRightClick();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.pendingSignEdits.containsKey(player));

        PlayerInteractEvent interactEvent = spy(mock(PlayerInteractEvent.class));

        when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(interactEvent.getClickedBlock()).thenReturn(block);
        when(interactEvent.getPlayer()).thenReturn(player);

        listener.onInteract(interactEvent);

        verify(sign).setLine(2, "xray yankee zulu");
    }

    @Test
    public void sayHintAfterCommandInClickingMode() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        doReturn(true).when(spyConfig).allowedToEditSignByRightClick();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(player).sendMessage(matches("(?i)^.*right.*click.*sign.*$"));
    }

    @Test
    public void sayHintWhenNotLookingAtSignAndNotAllowedToEditByRightClick() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(mock(Block.class));
        doReturn(false).when(spyConfig).allowedToEditSignByRightClick();

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertFalse(listener.pendingSignEdits.containsKey(player));
        verify(block, never()).getState();
        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*look.*$"));
        verify(sign, never()).setLine(2, "xray yankee zulu");
    }

    @Test
    public void clickingAutoEditsSignOnSight() {
        String argsString = "set 1 alpha bravo charlie";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        spyConfig.setClicking("auto");

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(sign).setLine(0, "alpha bravo charlie");
    }

    @Test
    public void clickingAutoQueuesEditForRightClickWithoutSight() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        spyConfig.setClicking("auto");

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.pendingSignEdits.containsKey(player));
        Assert.assertTrue(listener.pendingSignEdits.get(player).containsKey(2));
        Assert.assertTrue(listener.pendingSignEdits.get(player).get(2).equals("xray yankee zulu"));
    }

    @Test
    public void cancelPendingSignEditIfPlayerDisconnects() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        spyConfig.setClicking("auto");
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.pendingSignEdits.containsKey(player));

        listener.onDisconnect(event);

        Assert.assertFalse(listener.pendingSignEdits.containsKey(player));
    }

    @Test
    public void commandOpensUIWithSight() {
        String argsString = "ui";

        when(player.getTargetBlock(null, 10)).thenReturn(block);
        spyConfig.setClicking("auto");

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        verify(uiSignSubcommand).execute();
    }
}
