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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SignSideShim implements ISignSide {
    private final Object signSideLike;
    private final Class<?> signSideLikeClass;

    public SignSideShim(@NotNull Object signSideLike) {
        this.signSideLike = signSideLike;
        this.signSideLikeClass = signSideLike.getClass();
    }

    @NotNull
    public String[] getLines() {
        try {
            Method getLines = signSideLikeClass.getMethod("getLines");
            return (String[]) getLines.invoke(signSideLike);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected structure of Sign-like object");
        }
    }

    @NotNull
    public String getLine(int index) throws IndexOutOfBoundsException {
        try {
            Method getLine = signSideLikeClass.getMethod("getLine", int.class);
            return (String) getLine.invoke(signSideLike, index);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected structure of Sign-like object");
        }
    }

    public void setLine(int index, @NotNull String line) throws IndexOutOfBoundsException {
        try {
            Method setLine = signSideLikeClass.getMethod("setLine", int.class, String.class);
            setLine.invoke(signSideLike, index, line);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected structure of Sign-like object");
        }
    }
}
