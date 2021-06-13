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

package org.deltik.mc.signedit;

import org.bukkit.Bukkit;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CraftBukkitReflector {
    public String BUKKIT_SERVER_VERSION;

    @Inject
    public CraftBukkitReflector() {
        BUKKIT_SERVER_VERSION = getBukkitServerVersion();
    }

    private static String getBukkitServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public static Method getDeclaredMethodRecursive(Class<?> whateverClass, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        try {
            Method method = whateverClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Class<?> superClass = whateverClass.getSuperclass();
            if (superClass != null) {
                return getDeclaredMethodRecursive(superClass, name, parameterTypes);
            }
            throw e;
        }
    }

    public static Field getFirstFieldOfType(Object source, Class<?> desiredType) throws NoSuchFieldException {
        return getFirstFieldOfType(source.getClass(), desiredType);
    }

    public static Field getFirstFieldOfType(Class<?> source, Class<?> desiredType) throws NoSuchFieldException {
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
}
