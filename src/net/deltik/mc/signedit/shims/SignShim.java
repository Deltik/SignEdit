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

import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class SignShim {
    @NotNull
    private final Sign implementation;

    public SignShim(@NotNull Sign implementation) {
        this.implementation = implementation;
    }

    @NotNull
    public ISignSide getSide(@NotNull SideShim side) {
        try {
            Object rawGetSide = SignHelpers.rawGetSide(implementation, side.toString());
            if (rawGetSide == null) {
                return getFrontSignSide();
            }
            return new SignSideShim(rawGetSide);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Unable to call method: getSide", e);
        }
    }

    @NotNull
    public Sign getImplementation() {
        return implementation;
    }

    /**
     * Proxy this {@link Sign} as the front side only for Bukkit 1.19.4 and below
     *
     * @return The front sign side as the {@link SignSideShim} adapter
     */
    @NotNull
    private SignSideShim getFrontSignSide() {
        return new SignSideShim(implementation);
    }
}
