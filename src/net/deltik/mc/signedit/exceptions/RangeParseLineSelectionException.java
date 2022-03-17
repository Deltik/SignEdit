/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit.exceptions;

public class RangeParseLineSelectionException extends LineSelectionException {
    private final String wholeSelection;
    private final String badRange;

    public RangeParseLineSelectionException(String wholeSelection, String badRange) {
        this.wholeSelection = wholeSelection;
        this.badRange = badRange;
    }

    @Override
    public String getMessage() {
        return this.wholeSelection;
    }

    public String getBadRange() {
        return badRange;
    }
}
