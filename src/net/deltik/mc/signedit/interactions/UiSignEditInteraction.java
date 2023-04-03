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
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.exceptions.SignEditorInvocationException;
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.IntStream;

import static net.deltik.mc.signedit.CraftBukkitReflector.*;

public class UiSignEditInteraction implements SignEditInteraction {
    private final SignEditInteractionManager interactionManager;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignText signText;
    private final SignTextHistoryManager historyManager;
    private final SignCommand signCommand;

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
                if (signText.getTargetSign() == null) {
                    signCommand.onCommand(player, new Command(SignCommand.COMMAND_NAME) {
                        @Override
                        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                            return false;
                        }
                    }, "", new String[]{"cancel"});
                    return;
                }

                runEarlyEventTask(signChangeEvent);
            } else {
                runLateEventTask(signChangeEvent);
            }
            return;
        }

        assert signText.getTargetSign() != null;
        if (player != null) {
            formatSignForSave(player, signText.getTargetSign());
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

        if (signText.signChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        comms.compareSignText(signText);
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.importSign();
        this.player = player;

        formatSignForEdit(player, sign);
        interactionManager.setPendingInteraction(player, this);

        try {
            openSignEditor(player, sign);
        } catch (Exception e) {
            formatSignForSave(player, sign);
            throw new SignEditorInvocationException(e);
        }
    }

    /**
     * Try using the Bukkit 1.18+ stable API to open the sign editor and fall back to a reflection alternative
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @throws Exception if anything goes wrong while trying to open the sign editor
     */
    private void openSignEditor(Player player, Sign sign) throws Exception {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method method = Player.class.getMethod("openSign", Sign.class);
            method.invoke(player, sign);
        } catch (NoSuchMethodException ignored) {
            openSignEditorWithReflection(player, sign);
        }
    }

    /**
     * Take a reflection-based guess to open the sign editor for common CraftBukkit implementations prior to Bukkit 1.18
     * <p>
     * Prior to Bukkit 1.18, there was no stable API to open the sign editor.
     * Instead, there was a method called <code>EntityHuman#openSign</code> available since CraftBukkit 1.8, which took
     * a <code>TileEntitySign</code> as its argument.
     * This method tries to call that unstable API method.
     *
     * @param player The player that wants to open the sign editor
     * @param sign   The sign that should load into the player's sign editor
     * @throws Exception if anything goes wrong while trying to open the sign editor
     */
    private void openSignEditorWithReflection(Player player, Sign sign) throws Exception {
        Object tileEntitySign = toRawTileEntity(sign);
        Object entityPlayer = toRawEntity(player);

        makeTileEntitySignEditable(tileEntitySign);

        Method openSignMethod = findMethodByParameterTypes(
                entityPlayer.getClass(), tileEntitySign.getClass()
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
     * FIXME: Find a more reliable way than looking for the first public boolean to mark the TileEntitySign as editable
     */
    private void makeTileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = getFirstFieldOfType(tileEntitySign, boolean.class, Modifier.PUBLIC);
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
