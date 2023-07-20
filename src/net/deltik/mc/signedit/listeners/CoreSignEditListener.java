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

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import net.deltik.mc.signedit.interactions.UiSignEditInteraction;
import net.deltik.mc.signedit.interactions.WaxSignEditInteraction;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignHelpers;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Provider;

public class CoreSignEditListener extends SignEditListener {
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final SignEditInteractionManager interactionManager;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private final SignCommand signCommand;
    private final SignEditValidator signEditValidator;

    @Inject
    public CoreSignEditListener(
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            SignEditInteractionManager interactionManager,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            SignCommand signCommand,
            SignEditValidator signEditValidator
    ) {
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.interactionManager = interactionManager;
        this.commsBuilderProvider = commsBuilderProvider;
        this.signCommand = signCommand;
        this.signEditValidator = signEditValidator;
    }

    /**
     * Extract the {@link Sign} from the provided {@link BlockEvent}
     *
     * @param blockEvent A {@link BlockEvent} that has not yet been confirmed to return a {@link Sign} with a
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        } catch (NoSuchMethodError ignored) {
            // No-op in Bukkit 1.8.8 and older
        }
        Block maybeBlock = event.getClickedBlock();
        if (maybeBlock == null) return;

        BlockState maybeSign = maybeBlock.getState();
        if (!(maybeSign instanceof Sign)) return;

        Sign sign = (Sign) maybeSign;
        SignShim signAdapter = new SignShim(sign);
        Player player = event.getPlayer();

        try {
            if (interactionManager.isInteractionPending(player)) {
                event.setCancelled(true);
                SignEditInteraction interaction = interactionManager.removePendingInteraction(player);
                SideShim side = SideShim.fromRelativePosition(sign, player);
                interaction.interact(player, signAdapter, side);
            } else if (SignHelpers.isEditable(sign)) {
                overrideNativeBehavior(event, signAdapter);
            }
        } catch (Throwable e) {
            ChatComms comms = commsBuilderProvider.get().commandSender(player).build().comms();
            comms.reportException(e);
        }
    }

    /**
     * Override the Bukkit 1.20 native behavior of what a {@link Player} can do with a {@link Sign}
     *
     * @param event       The {@link PlayerInteractEvent} that triggered the interaction with the sign
     * @param signAdapter The {@link SignShim} implementation representing the sign being interacted with
     */
    private void overrideNativeBehavior(PlayerInteractEvent event, SignShim signAdapter) {
        Player player = event.getPlayer();
        if (player.isSneaking() && event.hasItem()) {
            return;
        }

        SignText signText = new SignText(signEditValidator);
        SignEditInteraction maybeSignEditInteraction = null;

        ItemStack eventItem = event.getItem();

        if (eventItem != null &&
                eventItem.getType().equals(Material.getMaterial("HONEYCOMB")) &&
                !event.useItemInHand().equals(Event.Result.DENY)
        ) {
            if (!player.hasPermission("signedit." + SignCommand.COMMAND_NAME + ".wax")) return;

            signText.setShouldBeEditable(false);
            maybeSignEditInteraction = new WaxSignEditInteraction(
                    signText,
                    commsBuilderProvider.get()
            );
        } else if (!event.useInteractedBlock().equals(Event.Result.DENY)) {
            if (!player.hasPermission("signedit." + SignCommand.COMMAND_NAME + ".ui")) return;

            maybeSignEditInteraction = new UiSignEditInteraction(
                    interactionManager,
                    commsBuilderProvider.get(),
                    signText,
                    historyManager,
                    signCommand
            );
        }

        if (maybeSignEditInteraction != null) {
            event.setCancelled(true);
            SideShim side = SideShim.fromRelativePosition(signAdapter.getImplementation(), player);
            maybeSignEditInteraction.interact(player, signAdapter, side);
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
