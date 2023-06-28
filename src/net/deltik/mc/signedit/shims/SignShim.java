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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class SignShim {
    @NotNull
    private final Sign implementation;

    public SignShim(@NotNull Sign implementation) {
        this.implementation = implementation;
    }

    @NotNull
    public ISignSide getSide(@NotNull SideShim side) {
        Method getSideMethod = Arrays.stream(Sign.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("getSide") && method.getParameterCount() == 1)
                .findFirst()
                .orElse(null);

        if (getSideMethod == null) {
            return new SignSideShim(implementation);
        }

        Class<?> parameterType = getSideMethod.getParameterTypes()[0];
        if (!parameterType.isEnum()) {
            throw new IllegalStateException("Unexpected structure of Sign");
        }

        try {
            Method enumValueOf = parameterType.getMethod("valueOf", String.class);
            Enum<?> enumValue = (Enum<?>) enumValueOf.invoke(null, side.name());
            return new SignSideShim(getSideMethod.invoke(implementation, enumValue));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to call method: getSide", e);
        }
    }

    @NotNull
    public Sign getImplementation() {
        return implementation;
    }
}
