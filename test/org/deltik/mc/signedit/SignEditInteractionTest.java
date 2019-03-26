package org.deltik.mc.signedit;

import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.UiSignEditInteraction;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({SignEditInteraction.class})
public class SignEditInteractionTest extends SignEditTest {
    @Test
    public void validatedCommitForbiddenSignEdit() throws Exception {
        SignEditInteraction spySignEditInteraction = spy(new UiSignEditInteraction(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        whenNew(SignChangeEvent.class).withAnyArguments().thenReturn(event);
        doReturn(true).when(event).isCancelled();
        doNothing().when(spySignEditInteraction).interact(player, sign);

        spySignEditInteraction.validatedInteract(player, sign);

        verify(spySignEditInteraction, never()).interact(player, sign);
        verify(player).sendMessage(matches("(?i)^.*edit.*forbidden.*$"));
    }

    @Test
    public void validatedCommitPermittedSignEdit() throws Exception {
        SignEditInteraction spySignEditInteraction = spy(new UiSignEditInteraction(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        whenNew(SignChangeEvent.class).withAnyArguments().thenReturn(event);
        doReturn(false).when(event).isCancelled();
        doNothing().when(spySignEditInteraction).interact(player, sign);

        spySignEditInteraction.validatedInteract(player, sign);

        verify(spySignEditInteraction).interact(player, sign);
    }

    @Test
    public void removeInProgressEditIfPlayerEditsSign() throws Exception {
        SignEditInteraction spySignEditInteraction = spy(new UiSignEditInteraction(null, null));
        SignChangeEvent event = mock(SignChangeEvent.class);
        when(event.getPlayer()).thenReturn(player);
        // Apply color
        when(event.getLines()).thenReturn(new String[0]);

        Assert.assertFalse(listener.isInProgress(player));

        listener.registerInProgressInteraction(player, spySignEditInteraction);

        Assert.assertTrue(listener.isInProgress(player));

        listener.onSignChange(event);

        Assert.assertFalse(listener.isInProgress(player));
    }

    @Test
    public void cleanupInProgressEditIfPlayerDisconnects() throws Exception {
        SignEditInteraction spySignEditInteraction = mock(UiSignEditInteraction.class);
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        Assert.assertFalse(listener.isInProgress(player));

        listener.registerInProgressInteraction(player, spySignEditInteraction);

        Assert.assertTrue(listener.isInProgress(player));

        listener.onDisconnect(event);

        Assert.assertFalse(listener.isInProgress(player));
        verify(spySignEditInteraction).cleanup();
    }
}
