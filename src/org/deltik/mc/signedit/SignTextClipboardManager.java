/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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

package org.deltik.mc.signedit;

import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignTextClipboardManager {
    private Map<Player, SignText> playerSignTextMap = new HashMap<>();

    @Inject
    public SignTextClipboardManager() {
    }

    public void forgetPlayer(Player player) {
        playerSignTextMap.remove(player);
    }

    public SignText getClipboard(Player player) {
        return playerSignTextMap.get(player);
    }

    public void setClipboard(Player player, SignText clipboard) {
        playerSignTextMap.put(player, clipboard);
    }
}
