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
import net.deltik.mc.signedit.SignTextHistoryManager;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class SetSignEditInteraction implements SignEditInteraction {
    private final SignText signText;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignTextHistoryManager historyManager;

    @Inject
    public SetSignEditInteraction(
            SignText signText,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignTextHistoryManager historyManager
    ) {
        this.signText = signText;
        this.commsBuilder = commsBuilder;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "change_sign_text";
    }

    @Override
    public void interact(Player player, SignShim sign, SideShim side) {
        signText.setTargetSign(sign, side);

        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        signText.applySignAutoWax(player, comms);
        if (signText.signTextChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }
}
