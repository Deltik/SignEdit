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

import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public enum SideShim {
    FRONT,
    BACK;

    public static SideShim fromRelativePosition(@NotNull Sign sign, @NotNull LivingEntity entity) {
        Vector vector = entity.getEyeLocation().toVector().subtract(sign.getLocation().add(0.5, 0.5, 0.5).toVector());
        BlockData blockData;
        try {
            blockData = sign.getBlockData();
        } catch (NoSuchMethodError e) {
            // Compatibility with Bukkit 1.12.2 and older
            return FRONT;
        }

        Vector signDirection = getSignDirection(blockData);
        return vector.dot(signDirection) > 0 ? FRONT : BACK;
    }

    @NotNull
    private static Vector getSignDirection(BlockData blockData) {
        BlockFace signFace;

        if (blockData instanceof Directional) {
            Directional directional = (Directional) blockData;
            signFace = directional.getFacing();
        } else if (blockData instanceof Rotatable) {
            Rotatable rotatable = (Rotatable) blockData;
            signFace = rotatable.getRotation();
        } else {
            signFace = BlockFace.NORTH;
        }

        return new Vector(signFace.getModX(), signFace.getModY(), signFace.getModZ());
    }
}
