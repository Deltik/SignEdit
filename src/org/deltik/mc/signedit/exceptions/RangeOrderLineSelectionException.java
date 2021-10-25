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

package org.deltik.mc.signedit.exceptions;

public class RangeOrderLineSelectionException extends LineSelectionException {
    private final String wholeSelection;
    private final String invalidLowerBound;
    private final String invalidUpperBound;

    public RangeOrderLineSelectionException(String wholeSelection, String invalidLowerBound, String invalidUpperBound) {
        this.wholeSelection = wholeSelection;
        this.invalidLowerBound = invalidLowerBound;
        this.invalidUpperBound = invalidUpperBound;
    }

    @Override
    public String getMessage() {
        return this.wholeSelection;
    }

    public String getInvalidLowerBound() {
        return invalidLowerBound;
    }

    public String getInvalidUpperBound() {
        return invalidUpperBound;
    }
}
