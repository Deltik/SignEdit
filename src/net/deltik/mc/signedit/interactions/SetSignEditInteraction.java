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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.SignTextHistoryManager;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class SetSignEditInteraction implements SignEditInteraction {
    private SignText signText;
    private final ChatComms comms;
    private final SignTextHistoryManager historyManager;

    @Inject
    public SetSignEditInteraction(SignText signText, ChatComms comms, SignTextHistoryManager historyManager) {
        this.signText = signText;
        this.comms = comms;
        this.historyManager = historyManager;
    }

    @Override
    public String getName() {
        return "change_sign_text";
    }

    @Override
    public void interact(Player player, Sign sign) {
        signText.setTargetSign(sign);
        signText.applySign();
        if (signText.signChanged()) {
            historyManager.getHistory(player).push(signText);
        }

        comms.compareSignText(signText);
    }
}
