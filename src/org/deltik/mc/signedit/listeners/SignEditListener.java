/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.ChatCommsModule;
import org.deltik.mc.signedit.SignTextClipboardManager;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.interactions.BookUiSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteraction;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignEditListener implements Listener {

    private SignTextClipboardManager clipboardManager;
    private SignTextHistoryManager historyManager;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;

    private Map<Player, SignEditInteraction> pendingInteractions = new HashMap<>();

    @Inject
    public SignEditListener(
            SignTextClipboardManager clipboardManager,
            SignTextHistoryManager historyManager,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider
    ) {
        this.clipboardManager = clipboardManager;
        this.historyManager = historyManager;
        this.commsBuilderProvider = commsBuilderProvider;
    }

    @EventHandler
    public void onRightClickSign(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();

        if (isInteractionPending(player)) {
            try {
                SignEditInteraction interaction = removePendingInteraction(player);
                interaction.interact(player, sign);
            } catch (Exception e) {
                ChatComms comms = commsBuilderProvider.get().player(player).build().comms();
                comms.reportException(e);
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        clipboardManager.forgetPlayer(player);
        historyManager.forgetPlayer(player);

        if (isInteractionPending(player)) {
            endInteraction(player, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChangeDoReformat(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (isInteractionPending(player)) {
            getPendingInteraction(player).cleanup(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChangeDoSaveResult(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (isInteractionPending(player)) {
            endInteraction(player, event);
        }
    }

    @EventHandler
    public void onSignChangeBookMode(PlayerEditBookEvent event) {
        Player player = event.getPlayer();

        if (getPendingInteraction(player) instanceof BookUiSignEditInteraction) {
            endInteraction(player, event);
        }
    }

    @EventHandler
    public void onLeaveSignEditorBook(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (getPendingInteraction(player) instanceof BookUiSignEditInteraction) {
            endInteraction(player, event);
        }
    }

    @EventHandler
    public void onDropSignEditorBook(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isInteractionPending(player)) return;

        ItemStack droppedItem = event.getItemDrop().getItemStack();
        Material droppedItemType = droppedItem.getType();
        if (droppedItemType == Material.WRITABLE_BOOK || droppedItemType == Material.WRITTEN_BOOK) {
            ItemStack item = event.getItemDrop().getItemStack();
            event.getItemDrop().remove();
            player.getInventory().setItemInMainHand(item);
            endInteraction(player, event);
        }
    }

    @EventHandler
    public void onClickSignEditorBook(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) return;
        Player player = (Player) human;
        if (!isInteractionPending(player)) return;

        endInteraction(player, event);
    }

    protected void endInteraction(Player player, Event event) {
        try {
            removePendingInteraction(player).cleanup(event);
        } catch (Exception e) {
            ChatComms comms = commsBuilderProvider.get().player(player).build().comms();
            comms.reportException(e);
        }
    }

    public void setPendingInteraction(Player player, SignEditInteraction interaction) {
        if (pendingInteractions.get(player) != null) {
            removePendingInteraction(player).cleanup();
        }
        pendingInteractions.put(player, interaction);
    }

    public boolean isInteractionPending(Player player) {
        return pendingInteractions.containsKey(player);
    }

    public SignEditInteraction removePendingInteraction(Player player) {
        return pendingInteractions.remove(player);
    }

    public SignEditInteraction getPendingInteraction(Player player) {
        return pendingInteractions.get(player);
    }
}
