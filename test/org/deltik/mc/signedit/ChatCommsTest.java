/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ChatCommsTest {
    @Test
    public void resourceBundleControlFallbackLocale() {
        Locale fallbackLocale = new Locale.Builder().setLanguageTag("el-GR").build();
        Locale badLocale = new Locale.Builder().setLanguageTag("hu-HU").build();
        ResourceBundle.Control control = new ChatComms.UTF8ResourceBundleControl(fallbackLocale);

        assertEquals(
                fallbackLocale,
                control.getFallbackLocale("Comms", badLocale)
        );
    }

    @Test
    public void resourceBundleControlFallbackLocaleNoInfiniteLoop() {
        Locale fallbackLocale = new Locale.Builder().setLanguageTag("en-US").build();
        Locale badLocale = new Locale.Builder().setLanguageTag("en-US").build();
        ResourceBundle.Control control = new ChatComms.UTF8ResourceBundleControl(fallbackLocale);

        assertNotEquals(
                fallbackLocale,
                control.getFallbackLocale("Comms", badLocale)
        );
    }
}
