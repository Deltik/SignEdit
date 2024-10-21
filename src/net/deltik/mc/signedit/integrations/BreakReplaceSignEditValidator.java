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

package net.deltik.mc.signedit.integrations;

import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.listeners.CoreSignEditListener;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

public class BreakReplaceSignEditValidator extends StandardSignEditValidator {
    @Inject
    public BreakReplaceSignEditValidator(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public void validate(SignShim proposedSign, SideShim side, Player player) {
        Sign signImplementation = proposedSign.getImplementation();
        validateBlockBreak(signImplementation, player);
        validateBlockPlace(signImplementation, player);
        super.validate(proposedSign, side, player);
    }

    @Override
    public void validate(SignChangeEvent signChangeEvent) {
        Sign sign = CoreSignEditListener.getPlacedSignFromBlockEvent(signChangeEvent);
        Player player = signChangeEvent.getPlayer();
        validateBlockBreak(sign, player);
        validateBlockPlace(sign, player);
        super.validate(signChangeEvent);
    }

    private void validateBlockBreak(Sign sign, Player player) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(sign.getBlock(), player);
        pluginManager.callEvent(blockBreakEvent);
        if (blockBreakEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
    }

    private void validateBlockPlace(Sign sign, Player player) {
        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
                sign.getBlock(),
                sign,
                getBlockAgainst(sign),
                player.getInventory().getItemInMainHand(),
                player,
                true,
                EquipmentSlot.HAND
        );
        pluginManager.callEvent(blockPlaceEvent);
        if (blockPlaceEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
    }

    @NotNull
    private Block getBlockAgainst(Sign sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof org.bukkit.block.data.type.WallSign) {
            return getBlockAgainstWallSign(sign.getBlock());
        } else if (blockData instanceof org.bukkit.block.data.type.Sign) {
            return getBlockBelow(sign.getBlock());
        } else if (isHangingSign(blockData)) {
            return getBlockAbove(sign.getBlock());
        } else if (isWallHangingSign(blockData)) {
            return getBlockAttachedToHangingSign(sign.getBlock());
        }
        throw new RuntimeException("Unsupported sign type with BlockData: " + blockData.getAsString());
    }

    @NotNull
    private Block getBlockAgainstWallSign(Block block) {
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof WallSign)) {
            throw new BlockStateNotPlacedException();
        }

        WallSign wallSign = (WallSign) blockData;
        return block.getRelative(wallSign.getFacing().getOppositeFace());
    }

    @NotNull
    private Block getBlockBelow(Block block) {
        return block.getRelative(BlockFace.DOWN);
    }

    @NotNull
    private Block getBlockAbove(Block block) {
        return block.getRelative(BlockFace.UP);
    }

    private boolean classImplements(Class<?> item, String interfaceName) {
        for (Class<?> x : item.getInterfaces()) {
            if (x.getName().equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bukkit 1.13-compatible way of checking if a sign is a Bukkit 1.20 HangingSign
     */
    private boolean isHangingSign(BlockData blockData) {
        return classImplements(blockData.getClass(), "org.bukkit.block.data.type.HangingSign");
    }

    /**
     * Bukkit 1.13-compatible way of checking if a sign is a Bukkit 1.20 WallHangingSign
     */
    private boolean isWallHangingSign(BlockData blockData) {
        return classImplements(blockData.getClass(), "org.bukkit.block.data.type.WallHangingSign");
    }

    @NotNull
    private Block getBlockAttachedToHangingSign(Block block) {
        BlockData blockData = block.getBlockData();
        if (!isWallHangingSign(blockData)) {
            throw new BlockStateNotPlacedException();
        }

        Directional directional = (Directional) blockData;
        BlockFace blockFace = directional.getFacing();

        Set<Block> leftAndRightBlocks = new HashSet<>();
        leftAndRightBlocks.add(block.getRelative(cartesianClockwise(blockFace)));
        leftAndRightBlocks.add(block.getRelative(cartesianCounterClockwise(blockFace)));

        for (Block adjacentBlock : leftAndRightBlocks) {
            if (adjacentBlock.getType().isSolid()) {
                return adjacentBlock;
            }
        }

        // Floating wall hanging sign; attachment to air is arbitrary
        return leftAndRightBlocks.iterator().next();
    }

    private BlockFace cartesianClockwise(BlockFace originalFace) {
        switch (originalFace) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                throw new RuntimeException("Unsupported BlockFace: " + originalFace.name());
        }
    }

    private BlockFace cartesianCounterClockwise(BlockFace originalFace) {
        switch (originalFace) {
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;
            default:
                throw new RuntimeException("Unsupported BlockFace: " + originalFace.name());
        }
    }
}
