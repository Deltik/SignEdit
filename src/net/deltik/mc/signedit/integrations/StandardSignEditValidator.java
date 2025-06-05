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

package net.deltik.mc.signedit.integrations;

import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.listeners.CoreSignEditListener;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class StandardSignEditValidator implements SignEditValidator {
    protected final PluginManager pluginManager;

    @Inject
    public StandardSignEditValidator(
            PluginManager pluginManager
    ) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void validate(SignShim proposedSign, SideShim side, Player player) {
        SignChangeEvent signChangeEvent;
        try {
            Constructor<?> constructor = Arrays.stream(SignChangeEvent.class.getConstructors())
                    .filter(c -> c.getParameterCount() == 4)
                    .filter(c -> {
                        Class<?>[] parameterTypes = c.getParameterTypes();
                        return parameterTypes[0].equals(Block.class)
                                && parameterTypes[1].equals(Player.class)
                                && parameterTypes[2].equals(String[].class)
                                && parameterTypes[3].isEnum();
                    })
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("No such constructor exists for SignChangeEvent"));

            Method enumValueOf = constructor.getParameterTypes()[3].getMethod("valueOf", String.class);
            Enum<?> enumValue = (Enum<?>) enumValueOf.invoke(null, side.name());

            signChangeEvent = (SignChangeEvent) constructor.newInstance(
                    proposedSign.getImplementation().getBlock(),
                    player,
                    proposedSign.getSide(side).getLines(),
                    enumValue
            );
        } catch (
                NoSuchMethodException |
                IllegalAccessException |
                InvocationTargetException |
                InstantiationException e
        ) {
            signChangeEvent = makeOldSignChangeEvent(proposedSign, side, player);
        }

        pluginManager.callEvent(signChangeEvent);
        validate(signChangeEvent);
    }

    /**
     * Creates a Bukkit 1.8-compatible {@link SignChangeEvent} without {@link Sign} sides
     *
     * @param proposedSign The {@link Sign} with new values that hasn't been updated with {@link Sign#update()} yet
     * @param side Which side of the {@link Sign} changed
     *             (must be {@link SideShim#FRONT} because that was the only side before Bukkit 1.20)
     * @param player The {@link Player} who intends to change the {@link Sign}
     * @return A new {@link SignChangeEvent} that should be sent to
     *         {@link PluginManager#callEvent(org.bukkit.event.Event)}
     */
    @SuppressWarnings("deprecation")
    @NotNull
    private SignChangeEvent makeOldSignChangeEvent(SignShim proposedSign, SideShim side, Player player) {
        if (side != SideShim.FRONT) {
            throw new IllegalArgumentException("Bug: Event support missing for editing back of sign");
        }
        return new SignChangeEvent(
                proposedSign.getImplementation().getBlock(),
                player,
                proposedSign.getSide(side).getLines()
        );
    }

    @Override
    public void validate(SignChangeEvent signChangeEvent) {
        Sign bukkitSign = CoreSignEditListener.getPlacedSignFromBlockEvent(signChangeEvent);
        SignShim proposedSign = new SignShim(bukkitSign);
        if (signChangeEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
        SideShim side = SideShim.fromSignChangeEvent(signChangeEvent);
        String[] newLines = signChangeEvent.getLines();
        for (int i = 0; i < newLines.length; i++) {
            proposedSign.getSide(side).setLine(i, newLines[i]);
        }
    }
}
