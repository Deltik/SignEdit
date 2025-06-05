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

package net.deltik.mc.signedit.shims;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.exceptions.ForbiddenWaxedSignEditException;
import net.deltik.mc.signedit.interactions.WaxSignEditInteraction;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SignHelpers {
    public static boolean hasSignSideFeature() {
        Class<Enum<?>> sideEnumClass = getSideEnumClass();
        if (sideEnumClass == null) {
            return false;
        }

        try {
            Method enumValueOf = sideEnumClass.getMethod("valueOf", String.class);
            String checkString = SideShim.BACK.toString();
            return enumValueOf.invoke(null, checkString).toString().equals(checkString);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return false;
        }
    }

    @Nullable
    public static Object rawGetSide(@NotNull Sign sign, @NotNull String side) throws InvocationTargetException {
        Method getSideMethod = getGetSideMethod();
        Class<Enum<?>> sideEnumClass = getSideEnumClass();
        if (getSideMethod == null || sideEnumClass == null) {
            return null;
        }

        try {
            Method enumValueOf = sideEnumClass.getMethod("valueOf", String.class);
            Object rawSide = enumValueOf.invoke(null, side);
            return getSideMethod.invoke(sign, rawSide);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    private static Method getGetSideMethod() {
        return Arrays.stream(Sign.class.getMethods())
                .filter(method -> method.getName().equals("getSide") && method.getParameterCount() == 1)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Class<Enum<?>> getSideEnumClass() {
        Method getSideMethod = getGetSideMethod();
        if (getSideMethod == null) {
            return null;
        }
        if (getSideMethod.getParameterCount() != 1) {
            return null;
        }
        Class<?> parameterType = getSideMethod.getParameterTypes()[0];
        if (!parameterType.isEnum()) {
            return null;
        }
        return (Class<Enum<?>>) parameterType;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static boolean hasWaxableFeature() {
        try {
            Sign.class.getMethod("isWaxed");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean isEditable(Sign sign) {
        try {
            Method isWaxedMethod = sign.getClass().getMethod("isWaxed");
            boolean isWaxed = (boolean) isWaxedMethod.invoke(sign);
            return !isWaxed;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Ignore broken "editable" flag of signs in Bukkit 1.19.4 and below
            return false;
        }
    }

    public static void setEditable(Sign sign, boolean editable) {
        try {
            Method setWaxedMethod = sign.getClass().getMethod("setWaxed", boolean.class);
            setWaxedMethod.invoke(sign, !editable);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            setEditableOldApi(sign, editable);
        }
    }

    private static void setEditableOldApi(Sign sign, boolean editable) {
        try {
            Method setWaxedMethod = sign.getClass().getMethod("setEditable", boolean.class);
            setWaxedMethod.invoke(sign, !editable);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Ignore broken "editable" flag of signs in Bukkit 1.13.1 and below
        }
    }

    /**
     * Refresh the block state of a {@link Sign} and returns the updated {@link Sign}
     *
     * @param sign The {@link Sign} to refresh the {@link BlockState} of
     * @return The updated {@link Sign} with the refreshed {@link BlockState}
     * @throws BlockStateNotPlacedException if the {@link BlockState} of is not placed or is null
     */
    public static Sign refreshBlockState(@Nullable Sign sign) {
        BlockState newBlockState;
        try {
            newBlockState = sign != null ? sign.getBlock().getState() : null;
        } catch (IllegalStateException ignored) {
            newBlockState = null;
        }

        if (newBlockState instanceof Sign && newBlockState.isPlaced()) {
            return (Sign) newBlockState;
        } else {
            throw new BlockStateNotPlacedException();
        }
    }

    /**
     * Check if a {@link Sign} needs to be rewaxed before editing it and handle the necessary actions to bypass waxing
     *
     * @param sign   The {@link Sign} that needs to be checked and potentially bypassed
     * @param player The {@link Player} attempting to edit the sign
     * @return A boolean value indicating if the {@link Sign} needs to be rewaxed after being bypassed
     * @throws ForbiddenWaxedSignEditException if the player does not have permission to bypass waxing
     */
    public static boolean bypassWaxBefore(@NotNull Sign sign, Player player) {
        if (!hasWaxableFeature()) return false;

        boolean needRewax = false;
        sign = refreshBlockState(sign);
        if (!SignHelpers.isEditable(sign)) {
            if (player.hasPermission("signedit." + SignCommand.COMMAND_NAME + ".unwax")) {
                SignHelpers.setEditable(sign, true);
                sign.update();
                needRewax = true;
            } else {
                throw new ForbiddenWaxedSignEditException();
            }
        }
        return needRewax;
    }

    /**
     * Rewax a {@link Sign} after it has been edited. Check if rewaxing is necessary before calling this method!
     *
     * @param sign   The {@link Sign} that needs to be rewaxed
     * @param player The {@link Player} who edited the sign
     * @return A boolean value indicating if the {@link Sign} was successfully rewaxed
     */
    private static boolean bypassWaxAfter(Sign sign, Player player) {
        if (!hasWaxableFeature()) return false;

        if (player.hasPermission("signedit." + SignCommand.COMMAND_NAME + ".wax")) {
            sign = refreshBlockState(sign);
            SignHelpers.setEditable(sign, false);
            sign.update();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Rewax a {@link Sign} after it has been edited. Check if rewaxing is necessary before calling this method!
     *
     * @param sign   The {@link Sign} that needs to be rewaxed
     * @param player The {@link Player} who edited the sign
     * @param comms  The {@link ChatComms} object used to notify the {@link Player}
     * @return A boolean value indicating if the {@link Sign} was successfully rewaxed
     */
    public static boolean bypassWaxAfter(Sign sign, Player player, ChatComms comms) {
        boolean success = bypassWaxAfter(sign, player);
        if (!success) {
            comms.tell(comms.t("bypass_wax_cannot_rewax"));
            WaxSignEditInteraction.playWaxOff(sign);
        }
        return success;
    }
}
