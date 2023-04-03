/*
 * Copyright (C) 2017-2023 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit.listeners;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.SignTextClipboardManager;
import net.deltik.mc.signedit.SignTextHistoryManager;
import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Provider;

public class CoreSignEditListener extends SignEditListener {
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final SignEditInteractionManager interactionManager;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;

    @Inject
    public CoreSignEditListener(
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            SignEditInteractionManager interactionManager,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider
    ) {
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.interactionManager = interactionManager;
        this.commsBuilderProvider = commsBuilderProvider;
    }

    /**
     * Extract the {@link Sign} from the provided {@link BlockEvent}
     *
     * @param blockEvent A {@link BlockEvent} that is has yet been confirmed to return a {@link Sign} with a
     *                   call to {@link BlockEvent#getBlock()}
     * @return The {@link Sign} from the provided {@link BlockEvent}
     * @throws BlockStateNotPlacedException if the {@link BlockEvent} does not provide a placed {@link Sign} block
     */
    public static Sign getPlacedSignFromBlockEvent(BlockEvent blockEvent) {
        BlockState maybeSign = blockEvent.getBlock().getState();
        if (!(maybeSign instanceof Sign && maybeSign.isPlaced())) {
            throw new BlockStateNotPlacedException();
        }

        return (Sign) maybeSign;
    }

    @EventHandler
    public void onRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        BlockState maybeSign = event.getClickedBlock().getState();
        if (!(maybeSign instanceof Sign)) return;

        Sign sign = (Sign) maybeSign;
        Player player = event.getPlayer();

        if (interactionManager.isInteractionPending(player)) {
            try {
                SignEditInteraction interaction = interactionManager.removePendingInteraction(player);
                interaction.interact(player, sign);
            } catch (Throwable e) {
                ChatComms comms = commsBuilderProvider.get().commandSender(player).build().comms();
                comms.reportException(e);
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        clipboardManager.forgetPlayer(player);
        historyManager.forgetPlayer(player);

        if (interactionManager.isInteractionPending(player)) {
            interactionManager.endInteraction(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChangeDoReformat(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (interactionManager.isInteractionPending(player)) {
            interactionManager.getPendingInteraction(player).cleanup(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChangeDoSaveResult(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (interactionManager.isInteractionPending(player)) {
            interactionManager.endInteraction(player, event);
        }
    }
}
