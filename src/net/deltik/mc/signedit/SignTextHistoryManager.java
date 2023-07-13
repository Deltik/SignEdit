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

package net.deltik.mc.signedit;

import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignTextHistoryManager {
    private final Map<Player, SignTextHistory> playerHistoryMap = new HashMap<>();

    @Inject
    public SignTextHistoryManager() {
    }

    public void forgetPlayer(Player player) {
        playerHistoryMap.remove(player);
    }

    public SignTextHistory getHistory(Player player) {
        SignTextHistory history = playerHistoryMap.get(player);
        if (history == null) {
            history = new SignTextHistory(player);
            playerHistoryMap.put(player, history);
        }
        return history;
    }
}
