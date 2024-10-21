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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CalculatedBlockHitResult implements IBlockHitResult {
    private final LivingEntity entity;
    private final Block targetBlock;
    private final Location targetBlockLocation;

    public CalculatedBlockHitResult(LivingEntity entity, int maxDistance) {
        this.entity = entity;
        this.targetBlock = entity.getTargetBlock(null, maxDistance);
        this.targetBlockLocation = targetBlock.getLocation();
    }

    @Override
    public Block getHitBlock() {
        return targetBlock;
    }

    @Override
    public BlockFace getHitBlockFace() {
        Vector direction = entity.getLocation().toVector().subtract(targetBlockLocation.toVector()).normalize();
        double x = direction.getX();
        double z = direction.getZ();

        if (Math.abs(x) > Math.abs(z)) {
            if (x > 0) {
                return BlockFace.WEST;
            } else {
                return BlockFace.EAST;
            }
        } else {
            if (z > 0) {
                return BlockFace.NORTH;
            } else {
                return BlockFace.SOUTH;
            }
        }
    }
}
