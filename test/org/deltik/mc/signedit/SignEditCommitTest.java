package org.deltik.mc.signedit;

import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.committers.SignEditCommit;
import org.deltik.mc.signedit.committers.UiSignEditCommit;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({SignEditCommit.class})
public class SignEditCommitTest extends SignEditTest {
    @Test
    public void validatedCommitForbiddenSignEdit() throws Exception {
        SignEditCommit spySignEditCommit = spy(new UiSignEditCommit(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        whenNew(SignChangeEvent.class).withAnyArguments().thenReturn(event);
        doReturn(true).when(event).isCancelled();
        doNothing().when(spySignEditCommit).commit(player, sign);

        spySignEditCommit.validatedCommit(player, sign);

        verify(spySignEditCommit, never()).commit(player, sign);
        verify(player).sendMessage(matches("(?i)^.*edit.*forbidden.*$"));
    }

    @Test
    public void validatedCommitPermittedSignEdit() throws Exception {
        SignEditCommit spySignEditCommit = spy(new UiSignEditCommit(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        whenNew(SignChangeEvent.class).withAnyArguments().thenReturn(event);
        doReturn(false).when(event).isCancelled();
        doNothing().when(spySignEditCommit).commit(player, sign);

        spySignEditCommit.validatedCommit(player, sign);

        verify(spySignEditCommit).commit(player, sign);
    }

    @Test
    public void removeInProgressEditIfPlayerEditsSign() throws Exception {
        SignEditCommit spySignEditCommit = spy(new UiSignEditCommit(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        when(event.getPlayer()).thenReturn(player);
        // Apply color
        when(event.getLines()).thenReturn(new String[0]);

        Assert.assertFalse(listener.isInProgress(player));

        listener.registerInProgressCommit(player, spySignEditCommit);

        Assert.assertTrue(listener.isInProgress(player));

        listener.onSignChange(event);

        Assert.assertFalse(listener.isInProgress(player));
    }

    @Test
    public void cleanupInProgressEditIfPlayerDisconnects() throws Exception {
        SignEditCommit spySignEditCommit = mock(UiSignEditCommit.class);
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        Assert.assertFalse(listener.isInProgress(player));

        listener.registerInProgressCommit(player, spySignEditCommit);

        Assert.assertTrue(listener.isInProgress(player));

        listener.onDisconnect(event);

        Assert.assertFalse(listener.isInProgress(player));
        verify(spySignEditCommit).cleanup();
    }
}
