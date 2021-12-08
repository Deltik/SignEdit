/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

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

    /**
     * Find the first method that accepts the provided parameters
     * <p>
     * Performs a breadth-first search up the class ancestry until there are no more parents
     *
     * @param whateverClass The class to check for a matching method
     * @param parameterTypes The parameter types that the method accepts
     * @return The first matching method
     * @throws NoSuchMethodException if no methods match
     */
    public static Method findMethodByParameterTypes(Class<?> whateverClass, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        try {
            for (Method maybeMethod : whateverClass.getMethods()) {
                Class<?>[] candidateParameterTypes = maybeMethod.getParameterTypes();
                if (Arrays.equals(parameterTypes, candidateParameterTypes)) {
                    maybeMethod.setAccessible(true);
                    return maybeMethod;
                }
            }
            throw new NoSuchMethodException();
        } catch (NoSuchMethodException e) {
            Class<?> superClass = whateverClass.getSuperclass();
            if (superClass != null) {
                return findMethodByParameterTypes(superClass, parameterTypes);
            }
            throw e;
        }
    }

    public static Field getFirstFieldOfType(Object source, Class<?> desiredType) throws NoSuchFieldException {
        return getFirstFieldOfType(source, desiredType, ~0x0);
    }

    public static Field getFirstFieldOfType(
            Object source,
            Class<?> desiredType,
            int modifierMask
    ) throws NoSuchFieldException {
        return getFirstFieldOfType(source.getClass(), desiredType, modifierMask);
    }

    public static Field getFirstFieldOfType(
            @NotNull Class<?> source,
            @NotNull Class<?> desiredType,
            int modifierMask
    ) throws NoSuchFieldException {
        Class<?> ancestor = source;
        while (ancestor != null) {
            Field[] fields = ancestor.getDeclaredFields();
            for (Field field : fields) {
                Class<?> candidateType = field.getType();
                if (desiredType.isAssignableFrom(candidateType) && (field.getModifiers() & modifierMask) > 0) {
                    field.setAccessible(true);
                    return field;
                }
            }
            ancestor = ancestor.getSuperclass();
        }
        throw new NoSuchFieldException("Cannot match " + desiredType.getName() + " in ancestry of " + source.getName());
    }
}
