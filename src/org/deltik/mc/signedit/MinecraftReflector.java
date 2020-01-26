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

import org.bukkit.Bukkit;

import javax.inject.Inject;

public class MinecraftReflector {
    public String MINECRAFT_SERVER_VERSION;

    @Inject
    public MinecraftReflector() {
        MINECRAFT_SERVER_VERSION = getMinecraftServerVersion();
    }

    private static String getMinecraftServerVersion() {
        String bukkitPackageName = Bukkit.getServer().getClass().getPackage().getName();
        return bukkitPackageName.substring(bukkitPackageName.lastIndexOf('.') + 1);
    }

    public Class<?> getMinecraftServerClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + MINECRAFT_SERVER_VERSION + "." + className);
    }
}
