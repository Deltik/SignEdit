/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit.listeners;

import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.ChatCommsModule;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteractionManager;

import javax.inject.Inject;
import javax.inject.Provider;

public class CoreSignEditListener extends SignEditListener {
    private final SignTextClipboardManager clipboardManager;
    private final SignTextHistoryManager historyManager;
    private final SignEditInteractionManager interactionManager;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private SignCommand signCommand;

    @Inject
    public CoreSignEditListener(
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            SignEditInteractionManager interactionManager,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            SignCommand signCommand
    ) {
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.interactionManager = interactionManager;
        this.commsBuilderProvider = commsBuilderProvider;
        this.signCommand = signCommand;
    }

    @EventHandler
    public void onRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();

        if (interactionManager.isInteractionPending(player)) {
            try {
                SignEditInteraction interaction = interactionManager.removePendingInteraction(player);
                interaction.interact(player, sign);
            } catch (Exception e) {
                ChatComms comms = commsBuilderProvider.get().player(player).build().comms();
                comms.reportException(e);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (interactionManager.isInteractionPending(player)) {
            signCommand.onCommand(player, new Command(SignCommand.COMMAND_NAME) {
                @Override
                public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                    return false;
                }
            }, "", new String[]{"cancel"});
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
