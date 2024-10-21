/*
 * Copyright (C) 2017-2024 Deltik <https://www.deltik.net/>
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

import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface ISignSide {
    /**
     * Gets the line of text at the specified index on this side of the sign.
     * <p>
     * For example, <code>getLine(0)</code> will return the first line of text.
     *
     * @param index Line number to get the text from, starting at 0
     * @return Text on the given line
     * @throws IndexOutOfBoundsException Thrown when the line does not exist
     */
    String getLine(int index) throws IndexOutOfBoundsException;

    /**
     * Gets all the lines of text currently on this side of the sign.
     *
     * @return Array of Strings containing each line of text
     */
    String[] getLines();

    /**
     * Sets the line of text at the specified index on this side of the sign.
     * <p>
     * For example, <code>setLine(0, "Line One")</code> will set the first
     * line of text to "Line One".
     *
     * @param index Line number to set the text at, starting from 0
     * @param line  New text to set at the specified index
     * @throws IndexOutOfBoundsException If the index is out of the range 0..3
     */
    void setLine(int index, String line) throws IndexOutOfBoundsException;

    /**
     * Gets whether this side of the sign has glowing text.
     *
     * @return if this side of the sign has glowing text
     */
    boolean isGlowingText();

    /**
     * Sets whether this side of the sign has glowing text.
     *
     * @param glowing if this side of the sign has glowing text
     */
    void setGlowingText(boolean glowing);

    /**
     * Gets the color of this object.
     * This may be null to represent the default color of an object if the object has a special default color.
     *
     * @return The {@link DyeColor} of this object.
     */
    @Nullable
    DyeColor getColor();

    /**
     * Sets the color of this object to the specified {@link DyeColor}.
     * This may be null to represent the default color of an object if the object has a special default color.
     *
     * @param color The color of the object as a {@link DyeColor}
     * @throws NullPointerException if argument is null and this implementation does not support null
     */
    void setColor(@Nullable DyeColor color);
}
