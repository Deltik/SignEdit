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

package net.deltik.mc.signedit.integrations;

import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;

public class BreakReplaceSignEditValidator extends StandardSignEditValidator {
    @Inject
    public BreakReplaceSignEditValidator(Player player, PluginManager pluginManager) {
        super(player, pluginManager);
    }

    @Override
    public void validate(Sign proposedSign) {
        validateBlockBreak(proposedSign);
        validateBlockPlace(proposedSign);
        super.validate(proposedSign);
    }

    @Override
    public void validate(SignChangeEvent signChangeEvent) {
        Sign sign = (Sign) signChangeEvent.getBlock().getState();
        validateBlockBreak(sign);
        validateBlockPlace(sign);
    }

    private void validateBlockBreak(Sign sign) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(sign.getBlock(), player);
        pluginManager.callEvent(blockBreakEvent);
        if (blockBreakEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
    }

    private void validateBlockPlace(Sign sign) {
        Block signBlock = sign.getBlock();
        BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
                signBlock,
                signBlock.getState(),
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

    private Block getBlockAgainst(Sign sign) {
        BlockData blockData = sign.getBlockData();
        if (blockData instanceof org.bukkit.block.data.type.WallSign) {
            return getBlockAgainstWallSign(sign.getBlock());
        } else if (blockData instanceof org.bukkit.block.data.type.Sign) {
            return getBlockBelow(sign.getBlock());
        }
        throw new RuntimeException("Unsupported sign type with BlockData: " + blockData.getAsString());
    }

    private Block getBlockAgainstWallSign(Block block) {
        WallSign blockData = (WallSign) block.getBlockData();
        return block.getRelative(blockData.getFacing().getOppositeFace());
    }

    private Block getBlockBelow(Block block) {
        return block.getRelative(BlockFace.DOWN);
    }
}