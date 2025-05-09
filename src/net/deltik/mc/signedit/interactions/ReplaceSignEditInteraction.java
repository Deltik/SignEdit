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
import net.deltik.mc.signedit.exceptions.EmptyMainHandException;
import net.deltik.mc.signedit.exceptions.NoSignInMainHandException;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.inject.Inject;

public class ReplaceSignEditInteraction implements SignEditInteraction {
    private final SignEditValidator validator;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignTextHistoryManager historyManager;
    private boolean locked = false;

    @Inject
    public ReplaceSignEditInteraction(
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
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        
        // Check if the player has an item in hand
        if (mainHandItem == null || mainHandItem.getType() == Material.AIR) {
            throw new EmptyMainHandException();
        }
        
        // Check if the player has a sign in their main hand
        Material newSignMaterial = mainHandItem.getType();
        if (!newSignMaterial.toString().endsWith("_SIGN")) {
            throw new NoSignInMainHandException();
        }
        
        Block block = sign.getBlock();
        BlockState blockState = block.getState();
        
        if (!(blockState instanceof Sign)) {
            return;
        }
        
        Sign signBlock = (Sign) blockState;
        BlockData originalBlockData = block.getBlockData().clone();
        Material originalSignMaterial = signBlock.getType();
        
        // Save the sign text before replacing
        SignText signText = new SignText(validator);
        signText.setTargetSign(sign, side);
        signText.importSign();
        
        // Get the necessary properties to preserve
        boolean isWallSign = false;
        BlockFace facing = null;
        
        if (originalBlockData instanceof Directional) {
            isWallSign = true;
            facing = ((Directional) originalBlockData).getFacing();
        }
        
        // Store original rotation for floor signs
        BlockFace rotation = null;
        if (originalBlockData instanceof Rotatable) {
            rotation = ((Rotatable) originalBlockData).getRotation();
        }
        
        // Get the location of the sign
        Location location = block.getLocation();
        World world = location.getWorld();
        
        // Remove the original sign
        block.setType(Material.AIR);
        
        // Place the new sign
        block.setType(newSignMaterial);
        
        // Restore the sign text
        SignShim newSign = new SignShim((Sign) block.getState());
        signText.setTargetSign(newSign, SideShim.FRONT);
        
        // If it was a wall sign, we need to make the new one a wall sign too
        BlockData newBlockData = block.getBlockData();
        if (isWallSign && facing != null && newBlockData instanceof Directional) {
            ((Directional) newBlockData).setFacing(facing);
            block.setBlockData(newBlockData);
        }
        
        // If it was a floor sign, restore the rotation
        if (rotation != null && newBlockData instanceof Rotatable) {
            ((Rotatable) newBlockData).setRotation(rotation);
            block.setBlockData(newBlockData);
        }
        
        // Apply the sign text
        signText.applySign();
        
        // Handle inventory management in survival mode
        if (player.getGameMode() == GameMode.SURVIVAL) {
            PlayerInventory inventory = player.getInventory();
            
            // Decrease the count of the sign in hand
            if (mainHandItem.getAmount() > 1) {
                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                inventory.setItemInMainHand(mainHandItem);
            } else {
                inventory.setItemInMainHand(new ItemStack(Material.AIR));
            }
            
            // Give the player back the original sign type
            ItemStack originalSignItem = new ItemStack(originalSignMaterial, 1);
            
            // First check if there's an existing stack to add to
            for (ItemStack item : inventory.getStorageContents()) {
                if (item != null && item.getType() == originalSignMaterial && item.getAmount() < item.getMaxStackSize()) {
                    item.setAmount(item.getAmount() + 1);
                    return;
                }
            }
            
            // If no existing stack, add to the first empty slot or drop if full
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(originalSignItem);
            } else {
                world.dropItemNaturally(player.getLocation(), originalSignItem);
            }
        }
        
        ChatComms comms = commsBuilder.commandSender(player).build().comms();
        comms.tell(comms.t("sign_material_replaced"));
    }
    
    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
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
            return comms.t("replace_lock_mode_hint");
        }
        return comms.t("right_click_sign_to_apply_action_hint");
    }

    @Override
    public String getName() {
        return "replace_sign_material";
    }
}
