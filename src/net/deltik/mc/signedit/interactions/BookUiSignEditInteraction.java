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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistoryManager;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;

public class BookUiSignEditInteraction implements SignEditInteraction {
    private final Plugin plugin;
    private final SignEditInteractionManager interactionManager;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;
    protected ItemStack originalItem;
    protected int originalItemIndex;
    protected Player player;

    @Inject
    public BookUiSignEditInteraction(
            Plugin plugin,
            SignEditInteractionManager interactionManager,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
        this.plugin = plugin;
        this.interactionManager = interactionManager;
        this.commsBuilder = commsBuilder;
        this.signText = signText;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "open_sign_editor";
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        interactionManager.setPendingInteraction(player, this);

        if (originalItem == null) {
            signText.setTargetSign(sign, side);
            signText.importSign();
            formatSignTextForEdit(signText);
            openSignEditor(player);
            return;
        }

        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        comms.tell(comms.t("right_click_air_to_open_sign_editor"));
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
        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        PlayerInventory inventory = player.getInventory();
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK, 1);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setDisplayName(comms.t("sign_editor_item_name"));
        bookMeta.setPages(String.join("\n", signText.getLines()));
        book.setItemMeta(bookMeta);
        originalItem = inventory.getItemInMainHand();
        originalItemIndex = inventory.getHeldItemSlot();
        inventory.setItemInMainHand(book);
        comms.tell(comms.t("right_click_air_to_open_sign_editor"));
        interactionManager.removePendingInteraction(player);
        interactionManager.setPendingInteraction(player, this);
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
        String[] newLines;
        if (meta.hasPages()) {
            String page = meta.getPage(1);
            newLines = page.split("\n");
        } else {
            newLines = new String[]{};
        }
        for (int i = 0; i < signText.getLines().length; i++) {
            String newLine;
            if (i >= newLines.length) {
                newLine = "";
            } else {
                newLine = newLines[i];
            }
            signText.setLine(i, newLine);
        }
        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        signText.applySignAutoWax(player, comms, signText::applySign);
        if (signText.signTextChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }

    private void cleanupInventoryClickEvent(InventoryClickEvent event) {
        if (originalItemIndex == event.getSlot()) {
            interactionManager.setPendingInteraction(player, this);
            event.setCancelled(true);
        }
    }

    protected void formatSignTextForEdit(SignText signText) {
        for (int i = 0; i < 4; i++) {
            signText.setLineLiteral(i, signText.getLineParsed(i));
        }
    }

}
