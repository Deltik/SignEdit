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
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

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

        Particle confirmationParticle;
        Sound confirmationSound;
        if (stagedEditable != realEditable) {
            throw new ForbiddenSignEditException();
        } else if (realEditable) {
            comms.tell(comms.t("wax_removed"));
            confirmationParticle = Particle.valueOf("WAX_OFF");
            confirmationSound = Sound.valueOf("BLOCK_SIGN_WAXED_INTERACT_FAIL");
        } else {
            comms.tell(comms.t("wax_applied"));
            confirmationParticle = Particle.valueOf("WAX_ON");
            confirmationSound = Sound.valueOf("ITEM_HONEYCOMB_WAX_ON");
        }

        Location signLocation = signImplementation.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(confirmationParticle, signLocation, 20, 0.3, 0.3, 0.3, 0.5);
        player.playSound(signLocation, confirmationSound, 1, 1);
    }

    @Override
    public String getName() {
        if (Boolean.TRUE.equals(signText.shouldBeEditable())) {
            return "unwax_sign";
        } else {
            return "wax_sign";
        }
    }
}
