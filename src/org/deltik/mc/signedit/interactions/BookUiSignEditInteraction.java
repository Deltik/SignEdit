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

package org.deltik.mc.signedit.interactions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;

public class BookUiSignEditInteraction implements SignEditInteraction {
    private final Plugin plugin;
    private final SignEditListener listener;
    private final ChatComms comms;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;
    protected ItemStack originalItem;
    protected int originalItemIndex;
    protected Player player;

    @Inject
    public BookUiSignEditInteraction(
            Plugin plugin,
            SignEditListener listener,
            ChatComms comms,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
        this.plugin = plugin;
        this.listener = listener;
        this.comms = comms;
        this.signText = signText;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "open_sign_editor";
    }

    @Override
    public void interact(Player player, Sign sign) {
        listener.setPendingInteraction(player, this);

        if (originalItem == null) {
            signText.setTargetSign(sign);
            signText.importSign();
            formatSignTextForEdit(signText);
            openSignEditor(player);
            return;
        }

        comms.tellPlayer(comms.t("right_click_air_to_open_sign_editor"));
    }

    @Override
    public String getActionHint(ChatComms comms) {
        if (originalItem == null) {
            return SignEditInteraction.super.getActionHint(comms);
        }
        return comms.t("right_click_air_to_apply_action_hint");
    }

    protected void openSignEditor(Player player) {
        this.player = player;
        PlayerInventory inventory = player.getInventory();
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK, 1);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setDisplayName(comms.t("sign_editor_item_name"));
        bookMeta.setPages(String.join("\n", signText.getLines()));
        book.setItemMeta(bookMeta);
        originalItem = inventory.getItemInMainHand();
        originalItemIndex = inventory.getHeldItemSlot();
        inventory.setItemInMainHand(book);
        comms.tellPlayer(comms.t("right_click_air_to_open_sign_editor"));
        listener.removePendingInteraction(player);
        listener.setPendingInteraction(player, this);
    }

    @Override
    public void cleanup(Event event) {
        if (originalItem == null) return;

        if (event instanceof InventoryClickEvent) cleanupInventoryClickEvent((InventoryClickEvent) event);
        player.getInventory().setItem(originalItemIndex, originalItem);

        if (!(event instanceof PlayerEditBookEvent)) {
            return;
        }

        PlayerEditBookEvent editBookEvent = (PlayerEditBookEvent) event;
        editBookEvent.setCancelled(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> player.getInventory().setItem(originalItemIndex, originalItem),
                0
        );
        BookMeta meta = editBookEvent.getNewBookMeta();
        String page = meta.getPage(1);
        String[] newLines = page.split("\n");
        for (int i = 0; i < signText.getLines().length; i++) {
            String newLine;
            if (i >= newLines.length) {
                newLine = "";
            } else {
                newLine = newLines[i];
            }
            signText.setLine(i, newLine);
        }
        signText.applySign();
        if (signText.signChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }

    private void cleanupInventoryClickEvent(InventoryClickEvent event) {
        if (originalItemIndex == event.getSlot()) {
            listener.setPendingInteraction(player, this);
            event.setCancelled(true);
        }
    }

    protected void formatSignTextForEdit(SignText signText) {
        for (int i = 0; i < 4; i++) {
            signText.setLineLiteral(i, signText.getLineParsed(i));
        }
    }

}
