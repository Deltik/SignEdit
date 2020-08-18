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

package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.MinecraftReflector;
import org.deltik.mc.signedit.SignText;
import org.deltik.mc.signedit.SignTextHistoryManager;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import org.deltik.mc.signedit.exceptions.SignEditorInvocationException;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.IntStream;

public class UiSignEditInteraction implements SignEditInteraction {
    private final MinecraftReflector reflector;
    private final SignEditListener listener;
    private final ChatComms comms;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;

    protected Player player;

    @Inject
    public UiSignEditInteraction(
            MinecraftReflector reflector,
            SignEditListener listener,
            ChatComms comms,
            SignText signText,
            SignTextHistoryManager historyManager
    ) {
        this.reflector = reflector;
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

        signText.stageSign();
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
        attachPlayerToSign(player, sign);
        Object position = getBlockPosition(sign.getBlock());
        Object packet = createPositionalPacket(position, "PacketPlayOutOpenSignEditor");
        sendPacketToPlayer(packet, player);
    }

    private Object getEntityPlayer(Player player) throws Exception {
        Field entityPlayerField = getFirstFieldOfType(player,
                reflector.getMinecraftServerClass("Entity"));
        return entityPlayerField.get(player);
    }

    private void sendPacketToPlayer(Object packet, Player player) throws Exception {
        Object entityPlayer = getEntityPlayer(player);
        Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        connection
                .getClass()
                .getDeclaredMethod("sendPacket", reflector.getMinecraftServerClass("Packet"))
                .invoke(connection, packet);
    }

    private Object createPositionalPacket(Object position, String typeOfPacket) throws Exception {
        return createPositionalPacket(position, reflector.getMinecraftServerClass(typeOfPacket));
    }

    private Object createPositionalPacket(Object position, Class<?> typeOfPacket) throws Exception {
        return typeOfPacket
                .getConstructor(position.getClass())
                .newInstance(position);
    }

    private Object getBlockPosition(Block block) throws Exception {
        return reflector.getMinecraftServerClass("BlockPosition")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());
    }

    private void attachPlayerToSign(Player player, Sign sign) throws Exception {
        Object entityPlayer = getEntityPlayer(player);
        Object tileEntitySign = getTileEntitySign(sign);

        maketileEntitySignEditable(tileEntitySign);

        boolean attachedPlayerToSign = false;
        NoSuchFieldException noSuchFieldException = null;

        try {
            attachPlayerUUIDToTileEntitySign(player.getUniqueId(), tileEntitySign);
            attachedPlayerToSign = true;
        } catch (NoSuchFieldException e) {
            noSuchFieldException = e;
        }

        try {
            attachEntityPlayerToTileEntitySign(entityPlayer, tileEntitySign);
            attachedPlayerToSign = true;
        } catch (NoSuchFieldException e) {
            noSuchFieldException = e;
        }

        if (!attachedPlayerToSign) {
            throw noSuchFieldException;
        }
    }

    /**
     * Attach an EntityPlayer directly to a TileEntitySign
     * <p>
     * This is how the vanilla Minecraft server expects Players to be attached to Signs.
     * CraftBukkit respects this behavior.
     *
     * @param entityPlayer
     * @param tileEntitySign
     */
    private void attachEntityPlayerToTileEntitySign(Object entityPlayer, Object tileEntitySign)
            throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Field signEntityHumanField = getFirstFieldOfType(tileEntitySign,
                reflector.getMinecraftServerClass("EntityHuman"));
        signEntityHumanField.setAccessible(true);
        signEntityHumanField.set(tileEntitySign, entityPlayer);
    }

    /**
     * Attach a Player's unique ID to a Sign
     * <p>
     * Workaround for behavior change introduced in the PaperMC fork of CraftBukkit:
     * https://github.com/PaperMC/Paper/commit/906684ff4f9413fda228122315fdf0fffa674a42
     * <p>
     * PaperMC chooses to store a Player's UUID as the Sign's editor rather than upstream's EntityHuman.
     *
     * @param playerUUID     UUID of the Player who is editing the Sign
     * @param tileEntitySign TileEntitySign representation of the Sign to be edited
     */
    private void attachPlayerUUIDToTileEntitySign(UUID playerUUID, Object tileEntitySign)
            throws NoSuchFieldException, IllegalAccessException {
        Field signEditorField = getFirstFieldOfType(tileEntitySign, UUID.class);
        signEditorField.setAccessible(true);
        signEditorField.set(tileEntitySign, playerUUID);
    }

    private Object getTileEntitySign(Sign sign) throws Exception {
        Field tileEntityField = getFirstFieldOfType(sign,
                reflector.getMinecraftServerClass("TileEntity"));
        return tileEntityField.get(sign);
    }

    private void maketileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
        signIsEditable.setAccessible(true);
        signIsEditable.set(tileEntitySign, true);
    }

    private Field getFirstFieldOfType(Object source, Class<?> desiredType) throws NoSuchFieldException {
        return getFirstFieldOfType(source.getClass(), desiredType);
    }

    private Field getFirstFieldOfType(Class<?> source, Class<?> desiredType) throws NoSuchFieldException {
        Class<?> ancestor = source;
        while (ancestor != null) {
            Field[] fields = ancestor.getDeclaredFields();
            for (Field field : fields) {
                Class<?> candidateType = field.getType();
                if (desiredType.isAssignableFrom(candidateType)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            ancestor = ancestor.getSuperclass();
        }
        throw new NoSuchFieldException("Cannot match " + desiredType.getName() + " in ancestry of " + source.getName());
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
