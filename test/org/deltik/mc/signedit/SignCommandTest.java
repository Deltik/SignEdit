package org.deltik.mc.signedit;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class SignCommandTest extends SignEditTest {
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

        Assert.assertFalse(listener.isInteractionPending(player));

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.isInteractionPending(player));

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

        Assert.assertFalse(listener.isInteractionPending(player));
        verify(block, never()).getState();
        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*look.*$"));
        verify(sign, never()).setLine(2, "xray yankee zulu");
    }

    @Test
    public void sayForbiddenOnEditWhileNotPermitted() {
        String argsString = "set 2 no permission";
        Player unauthorizedPlayer = mock(Player.class);
        when(unauthorizedPlayer.hasPermission(anyString())).thenReturn(false);

        signCommand.onCommand(unauthorizedPlayer, command, cString, argsString.split(" "));

        verify(sign, never()).setLine(anyInt(), anyString());
        verify(unauthorizedPlayer, atLeastOnce()).sendMessage(anyString());
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

        Assert.assertFalse(listener.isInteractionPending(player));

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.isInteractionPending(player));
        SignEditInteraction interaction = listener.popSignEditInteraction(player);
        Assert.assertFalse(listener.isInteractionPending(player));
        interaction.interact(player, sign);
        verify(sign).setLine(2, "xray yankee zulu");
    }

    @Test
    public void cancelPendingSignEditIfPlayerDisconnects() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        spyConfig.setClicking("auto");
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.isInteractionPending(player));

        listener.onDisconnect(event);

        Assert.assertFalse(listener.isInteractionPending(player));
    }

    @Test
    public void cancelPendingSignEditOnCancelSignSubcommand() {
        String argsString = "set 3 xray yankee zulu";

        when(player.getTargetBlock(null, 10)).thenReturn(null);
        spyConfig.setClicking("auto");

        Assert.assertFalse(listener.isInteractionPending(player));

        signCommand.onCommand(player, command, cString, argsString.split(" "));

        Assert.assertTrue(listener.isInteractionPending(player));

        signCommand.onCommand(player, command, cString, "cancel".split(" "));

        Assert.assertFalse(listener.isInteractionPending(player));
    }

    @Test
    public void sayNothingToCancelIfNothingToCancelSignSubcommand() {
        Assert.assertFalse(listener.isInteractionPending(player));
        signCommand.onCommand(player, command, cString, "cancel".split(" "));
        Assert.assertFalse(listener.isInteractionPending(player));
        verify(player, atLeastOnce()).sendMessage(matches("(?i)^.*no.*cancel.*$"));
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
