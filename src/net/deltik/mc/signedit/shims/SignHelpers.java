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

import org.bukkit.block.Sign;

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
}
