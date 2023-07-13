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

package net.deltik.mc.signedit.shims;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.exceptions.ForbiddenWaxedSignEditException;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SignHelpers {
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
            Method setWaxedMethod = sign.getClass().getMethod("setWaxed", boolean.class);
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
     * Check if a {@link Sign} needs to be rewaxed before editing it and handle the necessary actions to bypass waxing
     *
     * @param sign   The {@link Sign} that needs to be checked and potentially bypassed
     * @param player The {@link Player} attempting to edit the sign
     * @param comms  The {@link ChatComms} object used to notify the {@link Player}
     * @return A boolean value indicating if the {@link Sign} needs to be rewaxed after being bypassed
     * @throws ForbiddenWaxedSignEditException if the player does not have permission to bypass waxing
     */
    public static boolean bypassWaxBefore(@NotNull Sign sign, Player player, ChatComms comms) {
        boolean needRewax = bypassWaxBefore(sign, player);
        if (needRewax) {
            comms.tell(comms.t("bypass_wax_before"));
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
    public static boolean bypassWaxAfter(Sign sign, Player player) {
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
        if (success) {
            comms.tell(comms.t("bypass_wax_after"));
        } else {
            comms.tell(comms.t("bypass_wax_cannot_rewax"));
        }
        return success;
    }
}
