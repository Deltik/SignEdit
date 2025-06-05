/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.exceptions.SignEditorInvocationException;
import net.deltik.mc.signedit.shims.ISignSide;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignHelpers;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static net.deltik.mc.signedit.CraftBukkitReflector.*;

public class UiSignEditInteraction implements SignEditInteraction {
    private final SignEditInteractionManager interactionManager;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;
    private final SignCommand signCommand;
    private boolean needsRewax;

    protected Player player;

    @Inject
    public UiSignEditInteraction(
            SignEditInteractionManager interactionManager,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignText signText,
            SignTextHistoryManager historyManager,
            SignCommand signCommand
    ) {
        this.interactionManager = interactionManager;
        this.commsBuilder = commsBuilder;
        this.signText = signText;
        this.historyManager = historyManager;
        this.signCommand = signCommand;
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
            if (interactionManager.isInteractionPending(player)) {
                if (signText.getTargetSignSide() == null) {
                    signCommand.onCommand(player, new Command(SignCommand.COMMAND_NAME) {
                        @Override
                        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                            return false;
                        }
                    }, "", new String[]{"cancel"});
                    return;
                }

                runEarlyEventTask(signChangeEvent);
                if (this.needsRewax) {
                    ChatComms comms = commsBuilder.commandSender(player).build().comms();
                    boolean isRewaxed = SignHelpers.bypassWaxAfter(signText.getTargetSign(), player, comms);
                    if (isRewaxed) this.needsRewax = false;
                }
            } else {
                runLateEventTask(signChangeEvent);
            }
            return;
        }

        assert signText.getTargetSign() != null;
        assert signText.getTargetSignSide() != null;
        if (player != null) {
            formatSignForSave(player, signText.getTargetSign(), signText.getSide());
        }
    }

    protected void runEarlyEventTask(SignChangeEvent event) {
        signText.importPendingSignChangeEvent(event);
    }

    protected void runLateEventTask(SignChangeEvent event) {
        if (event.isCancelled()) {
            throw new ForbiddenSignEditException();
        }

        signText.importAuthoritativeSignChangeEvent(event);

        if (signText.signTextChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        comms.compareSignText(signText);
    }

    public void load(Player player, Sign sign, SideShim side) {
        this.player = player;
        signText.setTargetSign(sign, side);
        signText.importSign();
        this.needsRewax = SignHelpers.bypassWaxBefore(sign, player);
        formatSignForEdit(player, sign, side);
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        Sign signImpl = sign.getImplementation();
        load(player, signImpl, side);
        interactionManager.setPendingInteraction(player, this);

        try {
            openSignEditor(player, signImpl, side);
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            formatSignForSave(player, signImpl, side);
            throw new SignEditorInvocationException(e);
        }
    }

    /**
     * Try using the Bukkit 1.18+ stable API to open the sign editor and fall back to a reflection alternative
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @param side   The side of the sign to open (ignored in Bukkit 1.19.4 and older)
     * @throws NoSuchFieldException      if something goes wrong while trying to open the sign editor
     * @throws InvocationTargetException if something goes wrong while trying to open the sign editor
     * @throws NoSuchMethodException     if something goes wrong while trying to open the sign editor
     * @throws IllegalAccessException    if something goes wrong while trying to open the sign editor
     */
    private void openSignEditor(Player player, Sign sign, SideShim side)
            throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        try {
            if (openSignEditorBukkit1_20(player, sign, side)) return;

            openSignEditorBukkit1_18(player, sign);
        } catch (NoSuchMethodException ignored) {
            openSignEditorWithReflection(player, sign, side);
        }
    }

    /**
     * Open the sign editor for a player using the Bukkit 1.18 API
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @throws NoSuchMethodException     if the "openSign" method cannot be found in the Player class
     * @throws IllegalAccessException    if the "openSign" method cannot be accessed due to Java reflection restrictions
     * @throws InvocationTargetException if an exception occurs while invoking the "openSign" method
     */
    private static void openSignEditorBukkit1_18(Player player, Sign sign)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = Player.class.getMethod("openSign", Sign.class);
        method.invoke(player, sign);
    }

    /**
     * Open the sign editor for a player using the Bukkit 1.20 API
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @param side   The player's viewing side for the sign
     * @return true if the sign editor was successfully opened, false otherwise
     * @throws NoSuchMethodException     if the "openSign" method does not match the expected signature
     * @throws IllegalAccessException    if the "openSign" method cannot be accessed due to Java reflection restrictions
     * @throws InvocationTargetException if an exception occurs while invoking the "openSign" method
     */
    private static boolean openSignEditorBukkit1_20(Player player, Sign sign, SideShim side)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            setPlayerWhoMayEditSign(player, sign);
        } catch (NoSuchFieldException ignored) {
            // Ignore PaperMC implementation detail
        }

        Optional<Method> optionalMethod = Arrays.stream(player.getClass().getMethods())
                .filter(method -> method.getName().equals("openSign") && method.getParameterCount() == 2)
                .filter(method -> {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    return parameterTypes[0].equals(Sign.class) && parameterTypes[1].isEnum();
                }).findFirst();

        if (optionalMethod.isPresent()) {
            Method method = optionalMethod.get();
            Method enumValueOf = method.getParameterTypes()[1].getMethod("valueOf", String.class);
            Enum<?> enumValue = (Enum<?>) enumValueOf.invoke(null, side.name());
            method.invoke(player, sign, enumValue);
            return true;
        }
        return false;
    }

    /**
     * Work around the Spigot 1.20 limitation of not setting the player who may edit the sign
     * <p>
     * Bug report: <a href="https://hub.spigotmc.org/jira/browse/SPIGOT-7391">SPIGOT-7391</a>
     * <p>
     * FIXME: Find a more reliable way than looking for the first public UUID to assign the TileEntitySign to the Player
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     */
    private static void setPlayerWhoMayEditSign(Player player, Sign sign)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        Object tileEntitySign = toRawTileEntity(sign);

        Field playerWhoMayEdit = getFirstFieldOfType(tileEntitySign, UUID.class, Modifier.PUBLIC);
        playerWhoMayEdit.setAccessible(true);
        playerWhoMayEdit.set(tileEntitySign, player.getUniqueId());
    }

    /**
     * Take a reflection-based guess to open the sign editor for common CraftBukkit implementations that are missing a
     * Bukkit stable API method to open the sign editor, i.e., no <code>Player#openSign(Sign, Side)</code> method.
     * <p>
     * Prior to Bukkit 1.18, there was no stable API to open the sign editor.
     * Instead, there was a method called <code>EntityHuman#openSign(TileEntitySign)</code> available since CraftBukkit
     * 1.8.
     * <p>
     * CraftBukkit 1.20 replaced that method with <code>EntityHuman#openSign(TileEntitySign, boolean)</code> where the
     * boolean parameter indicates the side of the sign to open: <code>true</code> for the front and <code>false</code>
     * for the back.
     * <p>
     * This method tries to call one of those unstable API methods.
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @param side   The side of the sign to open (unused in Bukkit 1.19.4 and older)
     * @throws NoSuchFieldException      if something goes wrong while trying to open the sign editor
     * @throws InvocationTargetException if something goes wrong while trying to open the sign editor
     * @throws NoSuchMethodException     if something goes wrong while trying to open the sign editor
     * @throws IllegalAccessException    if something goes wrong while trying to open the sign editor
     */
    private void openSignEditorWithReflection(Player player, Sign sign, SideShim side)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        Object tileEntitySign = toRawTileEntity(sign);
        Object entityPlayer = toRawEntity(player);

        try {
            makeTileEntitySignEditable(tileEntitySign);
        } catch (NoSuchFieldException e) {
            setPlayerWhoMayEditSign(player, sign);
        }

        try {
            Method openSignMethod = findMethodByParameterTypes(
                    entityPlayer.getClass(), tileEntitySign.getClass()
            );
            openSignMethod.invoke(entityPlayer, tileEntitySign);
        } catch (NoSuchMethodException e) {
            Method openSignMethod = findMethodByParameterTypes(
                    entityPlayer.getClass(), tileEntitySign.getClass(), boolean.class
            );
            openSignMethod.invoke(entityPlayer, tileEntitySign, SideShim.FRONT.equals(side));
        }
    }

    private static Object toRawEntity(Entity entity)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return getDeclaredMethodRecursive(entity.getClass(), "getHandle").invoke(entity);
    }

    private static Object toRawTileEntity(BlockState blockState)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return getDeclaredMethodRecursive(blockState.getClass(), "getTileEntity").invoke(blockState);
    }

    /**
     * FIXME: Find a more reliable way than looking for the first public boolean to mark the TileEntitySign as editable
     */
    private void makeTileEntitySignEditable(Object tileEntitySign) throws NoSuchFieldException, IllegalAccessException {
        Field signIsEditable = getFirstFieldOfType(tileEntitySign, boolean.class, Modifier.PUBLIC);
        signIsEditable.setAccessible(true);
        signIsEditable.set(tileEntitySign, true);
    }

    private void formatSignForEdit(Player player, Sign sign, SideShim side) {
        SignShim signShim = new SignShim(sign);
        ISignSide signSide = signShim.getSide(side);
        IntStream.range(0, 4).forEach(i -> signSide.setLine(i, signText.getLineParsed(i)));
        try {
            sendSignUpdate(player, sign, signSide);
        } finally {
            IntStream.range(0, 4).forEach(i -> signSide.setLine(i, signText.getLine(i)));
        }
    }

    private void formatSignForSave(Player player, Sign sign, SideShim side) {
        SignShim signShim = new SignShim(sign);
        ISignSide signSide = signShim.getSide(side);
        IntStream.range(0, 4).forEach(i -> signSide.setLine(i, signText.getLine(i)));
        sendSignUpdate(player, sign, signSide);
    }

    private static void sendSignUpdate(Player player, Sign sign, ISignSide signSide) {
        try {
            for (Method method : player.getClass().getMethods()) {
                if (method.getName().equals("sendBlockUpdate") && method.getParameterCount() == 2) {
                    method.invoke(player, sign.getLocation(), sign);
                    return;
                }
            }
            throw new NoSuchMethodException();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            player.sendSignChange(sign.getLocation(), signSide.getLines());
        }
    }
}
