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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.*;
import net.deltik.mc.signedit.exceptions.NotAFloorSignException;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.inject.Inject;

public class RotateSignEditInteraction implements SignEditInteraction {
    public enum Direction {
        NORTH(180),
        NORTH_EAST(225),
        EAST(270),
        SOUTH_EAST(315),
        SOUTH(0),
        SOUTH_WEST(45),
        WEST(90),
        NORTH_WEST(135);

        private final int yaw;

        Direction(int yaw) {
            this.yaw = yaw;
        }

        public int getYaw() {
            return yaw;
        }
        
        public static Direction fromYaw(float yaw) {
            // Normalize yaw to 0-360
            while (yaw < 0) yaw += 360;
            while (yaw >= 360) yaw -= 360;
            
            // Get the closest direction
            if (yaw >= 337.5 || yaw < 22.5) return SOUTH;
            if (yaw >= 22.5 && yaw < 67.5) return SOUTH_WEST;
            if (yaw >= 67.5 && yaw < 112.5) return WEST;
            if (yaw >= 112.5 && yaw < 157.5) return NORTH_WEST;
            if (yaw >= 157.5 && yaw < 202.5) return NORTH;
            if (yaw >= 202.5 && yaw < 247.5) return NORTH_EAST;
            if (yaw >= 247.5 && yaw < 292.5) return EAST;
            return SOUTH_EAST;
        }
    }
    
    private final SignEditValidator validator;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignTextHistoryManager historyManager;
    
    private Direction targetDirection = null;
    private Integer relativeRotation = null;
    private Integer absoluteRotation = null;
    private boolean locked = false;

    @Inject
    public RotateSignEditInteraction(
            SignEditValidator validator,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignTextHistoryManager historyManager
    ) {
        this.validator = validator;
        this.commsBuilder = commsBuilder;
        this.historyManager = historyManager;
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        Block block = sign.getBlock();
        BlockData blockData = block.getBlockData();
        
        // Check if the sign is a floor sign
        if (!(blockData instanceof Rotatable)) {
            throw new NotAFloorSignException();
        }
        
        Rotatable rotatableData = (Rotatable) blockData.clone();
        org.bukkit.block.data.type.Sign signBlockData = (org.bukkit.block.data.type.Sign) rotatableData;
        
        // Make sure it's a floor sign and not a wall sign
        if (!signBlockData.isWallSign()) {
            ChatComms comms = commsBuilder.commandSender(player).build().comms();
            
            // Calculate the new rotation
            org.bukkit.block.BlockFace currentFacing = rotatableData.getRotation();
            org.bukkit.block.BlockFace newFacing = calculateNewFacing(currentFacing, player);
            
            if (newFacing != currentFacing) {
                rotatableData.setRotation(newFacing);
                
                // Save the sign text
                SignText signText = new SignText(validator);
                signText.setTargetSign(sign, side);
                
                // Apply the rotation
                block.setBlockData(rotatableData);
                
                if (signText.signTextChanged()) {
                    historyManager.getHistory(player).push(signText);
                }
                
                comms.tell(comms.t("sign_rotated"));
            } else {
                comms.tell(comms.t("sign_rotation_unchanged"));
            }
        } else {
            throw new NotAFloorSignException();
        }
    }
    
    private org.bukkit.block.BlockFace calculateNewFacing(org.bukkit.block.BlockFace currentFacing, Player player) {
        // Convert BlockFace to yaw
        int currentYaw = blockFaceToYaw(currentFacing);
        int newYaw = currentYaw;
        
        if (targetDirection != null) {
            // Set to specified direction
            newYaw = targetDirection.getYaw();
        } else if (relativeRotation != null) {
            // Apply relative rotation
            newYaw = (currentYaw + (relativeRotation * 45)) % 360;
            if (newYaw < 0) newYaw += 360;
        } else if (absoluteRotation != null) {
            // Set to absolute rotation (0-15)
            newYaw = (absoluteRotation * 22.5) % 360;
        } else {
            // Use player's direction
            newYaw = (int) player.getLocation().getYaw();
            // Convert to 0-360 range and adjust for Minecraft's yaw system
            newYaw = (newYaw + 180) % 360;
            if (newYaw < 0) newYaw += 360;
        }
        
        // Convert yaw back to BlockFace
        return yawToBlockFace(newYaw);
    }
    
    private int blockFaceToYaw(org.bukkit.block.BlockFace face) {
        switch (face) {
            case SOUTH: return 0;
            case SOUTH_WEST: return 45;
            case WEST: return 90;
            case NORTH_WEST: return 135;
            case NORTH: return 180;
            case NORTH_EAST: return 225;
            case EAST: return 270;
            case SOUTH_EAST: return 315;
            default: return 0;
        }
    }
    
    private org.bukkit.block.BlockFace yawToBlockFace(int yaw) {
        // Normalize yaw to 0-360
        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;
        
        // Get the closest facing direction
        if (yaw >= 337.5 || yaw < 22.5) return org.bukkit.block.BlockFace.SOUTH;
        if (yaw >= 22.5 && yaw < 67.5) return org.bukkit.block.BlockFace.SOUTH_WEST;
        if (yaw >= 67.5 && yaw < 112.5) return org.bukkit.block.BlockFace.WEST;
        if (yaw >= 112.5 && yaw < 157.5) return org.bukkit.block.BlockFace.NORTH_WEST;
        if (yaw >= 157.5 && yaw < 202.5) return org.bukkit.block.BlockFace.NORTH;
        if (yaw >= 202.5 && yaw < 247.5) return org.bukkit.block.BlockFace.NORTH_EAST;
        if (yaw >= 247.5 && yaw < 292.5) return org.bukkit.block.BlockFace.EAST;
        return org.bukkit.block.BlockFace.SOUTH_EAST;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public void setTargetDirection(Direction direction) {
        this.targetDirection = direction;
        this.relativeRotation = null;
        this.absoluteRotation = null;
    }
    
    public void setRelativeRotation(int steps) {
        this.relativeRotation = steps;
        this.targetDirection = null;
        this.absoluteRotation = null;
    }
    
    public void setAbsoluteRotation(int rotation) {
        this.absoluteRotation = rotation;
        this.targetDirection = null;
        this.relativeRotation = null;
    }
    
    @Override
    public void cleanup(Event event) {
        // If locked, don't clean up
        if (locked) {
            return;
        }
        // Otherwise, perform normal cleanup
    }
    
    @Override
    public String getActionHint(ChatComms comms) {
        if (locked) {
            return comms.t("rotate_lock_mode_hint");
        }
        return comms.t("right_click_sign_to_apply_action_hint");
    }

    @Override
    public String getName() {
        return "rotate_sign";
    }
}
