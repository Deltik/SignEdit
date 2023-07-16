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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignHelpers;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

public class WaxSignEditInteraction implements SignEditInteraction {
    private final SignText signText;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;

    @Inject
    public WaxSignEditInteraction(
            SignText signText,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder
    ) {
        this.signText = signText;
        this.commsBuilder = commsBuilder;
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        signText.setTargetSign(sign, side);

        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        Sign signImplementation = sign.getImplementation();
        BlockState realSign = signImplementation.getBlock().getState();
        if (!(realSign instanceof Sign)) {
            throw new BlockStateNotPlacedException();
        }

        boolean stagedEditable = Boolean.TRUE.equals(signText.shouldBeEditable());
        boolean realEditable = SignHelpers.isEditable((Sign) realSign);

        if (stagedEditable == realEditable) {
            comms.tell(comms.t("sign_did_not_change"));
            return;
        }

        signText.applySign(player);

        realSign = signImplementation.getBlock().getState();
        realEditable = SignHelpers.isEditable((Sign) realSign);

        if (stagedEditable != realEditable) {
            throw new ForbiddenSignEditException();
        } else if (realEditable) {
            comms.tell(comms.t("wax_removed"));
            playWaxOff(signImplementation);
        } else {
            comms.tell(comms.t("wax_applied"));
            playWaxOn(signImplementation);
        }
    }

    @Override
    public String getName() {
        if (Boolean.TRUE.equals(signText.shouldBeEditable())) {
            return "unwax_sign";
        } else {
            return "wax_sign";
        }
    }

    /**
     * Send a firework-like burst initiating from the middle of the specified {@link BlockState} to its {@link World}
     *
     * @param block    The block state from where the burst should originate
     * @param particle The kind of particle to spawn in the firework, or none if null
     * @param sound    The sound to play at the block, or none if null
     * @throws BlockStateNotPlacedException if the block state is not placed or somehow isn't in the {@link World}
     */
    private static void sendAudioVisualBurst(
            @NotNull BlockState block,
            @Nullable Particle particle,
            @Nullable Sound sound
    ) {
        Location signLocation = block.getLocation().add(0.5, 0.5, 0.5);
        World world = signLocation.getWorld();
        if (world == null || !block.isPlaced()) {
            throw new BlockStateNotPlacedException();
        }
        if (particle != null) {
            world.spawnParticle(particle, signLocation, 20, 0.3, 0.3, 0.3, 0.5);
        }
        if (sound != null) {
            world.playSound(signLocation, sound, 1, 1);
        }
    }

    /**
     * Play the "wax off" animation on the specified {@link Sign} by spawning particle effects and playing a sound
     *
     * @param sign The sign on which to play the "wax off" animation
     */
    public static void playWaxOff(Sign sign) {
        Particle confirmationParticle = Particle.valueOf("WAX_OFF");
        Sound confirmationSound = Sound.valueOf("BLOCK_SIGN_WAXED_INTERACT_FAIL");
        sendAudioVisualBurst(sign, confirmationParticle, confirmationSound);
    }

    /**
     * Play the "wax on" animation on the specified {@link Sign} by spawning particle effects and playing a sound
     *
     * @param sign The sign on which to play the "wax on" animation
     */
    public static void playWaxOn(Sign sign) {
        Particle confirmationParticle = Particle.valueOf("WAX_ON");
        Sound confirmationSound = Sound.valueOf("ITEM_HONEYCOMB_WAX_ON");
        sendAudioVisualBurst(sign, confirmationParticle, confirmationSound);
    }
}
