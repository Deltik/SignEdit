/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.deltik.mc.signedit.interactions.BookUiSignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteractionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BookUiSignEditListener extends SignEditListener {
    private final SignEditInteractionManager interactionManager;

    @Inject
    public BookUiSignEditListener(
            SignEditInteractionManager interactionManager
    ) {
        this.interactionManager = interactionManager;
    }

    @EventHandler
    public void onSignChangeBookMode(PlayerEditBookEvent event) {
        Player player = event.getPlayer();

        if (interactionManager.getPendingInteraction(player) instanceof BookUiSignEditInteraction) {
            interactionManager.endInteraction(player, event);
        }
    }

    @EventHandler
    public void onLeaveSignEditorBook(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (interactionManager.getPendingInteraction(player) instanceof BookUiSignEditInteraction) {
            interactionManager.endInteraction(player, event);
        }
    }

    @EventHandler
    public void onDropSignEditorBook(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!interactionManager.isInteractionPending(player)) return;

        ItemStack droppedItem = event.getItemDrop().getItemStack();
        Material droppedItemType = droppedItem.getType();
        if (droppedItemType == Material.WRITABLE_BOOK || droppedItemType == Material.WRITTEN_BOOK) {
            ItemStack item = event.getItemDrop().getItemStack();
            event.getItemDrop().remove();
            player.getInventory().setItemInMainHand(item);
            interactionManager.endInteraction(player, event);
        }
    }

    @EventHandler
    public void onClickSignEditorBook(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) return;
        Player player = (Player) human;
        if (!interactionManager.isInteractionPending(player)) return;

        interactionManager.endInteraction(player, event);
    }

}
