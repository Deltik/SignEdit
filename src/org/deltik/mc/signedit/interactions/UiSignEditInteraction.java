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

import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import org.deltik.mc.signedit.exceptions.SignEditorInvocationException;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

import static org.deltik.mc.signedit.CraftBukkitReflector.getDeclaredMethodRecursive;
import static org.deltik.mc.signedit.CraftBukkitReflector.getFirstFieldOfType;

public class UiSignEditInteraction implements SignEditInteraction {
    private final SignEditListener listener;
    private final ChatComms comms;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;

    protected Player player;

    @Inject
    public UiSignEditInteraction(
            SignEditListener listener,
            ChatComms comms,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
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
    public void cleanup(Event event) {
        if (event instanceof SignChangeEvent) {
            SignChangeEvent signChangeEvent = (SignChangeEvent) event;
            Player player = signChangeEvent.getPlayer();
            if (listener.isInteractionPending(player)) {
                runEarlyEventTask(signChangeEvent);
            } else {
                runLateEventTask(signChangeEvent);
            }
            return;
        }
        if (player != null) {
            formatSignForSave(player, signText.getTargetSign());
        }
    }

    protected void runEarlyEventTask(SignChangeEvent event) {
        String[] lines = event.getLines();
        Sign originalSign = signText.getTargetSign();
        for (int i = 0; i < lines.length; i++) {
            originalSign.setLine(i, signText.getLine(i));
            signText.setLine(i, lines[i]);
            event.setLine(i, signText.getLine(i));
        }
    }

    protected void runLateEventTask(SignChangeEvent event) {
        if (event.isCancelled()) {
            throw new ForbiddenSignEditException();
        }

        signText.importAuthoritativeSignChangeEvent(event);
        if (signText.signChanged()) {
            historyManager.getHistory(event.getPlayer()).push(signText);
        }

        comms.compareSignText(signText);
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.importSign();
        this.player = player;

        formatSignForEdit(player, sign);
        listener.setPendingInteraction(player, this);

        try {
            openSignEditor(player, sign);
        } catch (Exception e) {
            formatSignForSave(player, sign);
            throw new SignEditorInvocationException(e);
        }
    }

    private void openSignEditor(Player player, Sign sign) throws Exception {
        Object tileEntitySign = toRawTileEntity(sign);
        Object entityPlayer = toRawEntity(player);

        makeTileEntitySignEditable(tileEntitySign);

        Method openSignMethod = getDeclaredMethodRecursive(
                entityPlayer.getClass(), "openSign", tileEntitySign.getClass()
        );
        openSignMethod.invoke(entityPlayer, tileEntitySign);
    }

    private Object toRawEntity(Entity entity) throws Exception {
        return getDeclaredMethodRecursive(entity.getClass(), "getHandle").invoke(entity);
    }

    private Object toRawTileEntity(BlockState blockState) throws Exception {
        return getDeclaredMethodRecursive(blockState.getClass(), "getTileEntity").invoke(blockState);
    }

    /**
     * FIXME: Find a more reliable way than looking for the first boolean to mark the TileEntitySign as editable
     */
    private void makeTileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = getFirstFieldOfType(tileEntitySign, boolean.class);
        signIsEditable.setAccessible(true);
        signIsEditable.set(tileEntitySign, true);
    }

    private void formatSignForEdit(Player player, Sign sign) {
        String[] parsedLines = IntStream.range(0, 4).mapToObj(signText::getLineParsed).toArray(String[]::new);
        player.sendSignChange(sign.getLocation(), parsedLines);
    }

    private void formatSignForSave(Player player, Sign sign) {
        String[] originalLines = IntStream.range(0, 4).mapToObj(signText::getLine).toArray(String[]::new);
        player.sendSignChange(sign.getLocation(), originalLines);
    }
}
