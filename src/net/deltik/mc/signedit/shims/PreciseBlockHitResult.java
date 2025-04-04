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

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.lang.reflect.InvocationTargetException;

public class PreciseBlockHitResult implements IBlockHitResult {
    private final Object rayTraceResultLike;
    private final Class<?> rayTraceResultLikeClass;

    public PreciseBlockHitResult(Object rayTraceResultLike) {
        this.rayTraceResultLike = rayTraceResultLike;
        if (rayTraceResultLike == null) {
            this.rayTraceResultLikeClass = null;
        } else {
            this.rayTraceResultLikeClass = rayTraceResultLike.getClass();
        }
    }

    @Override
    public Block getHitBlock() {
        if (rayTraceResultLike == null) {
            return null;
        }

        try {
            return (Block) rayTraceResultLikeClass.getMethod("getHitBlock").invoke(rayTraceResultLike);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Unexpected structure of RayTraceResult-like object");
        }
    }

    @Override
    public BlockFace getHitBlockFace() {
        if (rayTraceResultLike == null) {
            return null;
        }

        try {
            return (BlockFace) rayTraceResultLikeClass.getMethod("getHitBlockFace").invoke(rayTraceResultLike);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Unexpected structure of RayTraceResult-like object");
        }
    }
}
