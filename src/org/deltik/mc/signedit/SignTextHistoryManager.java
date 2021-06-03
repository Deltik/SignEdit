/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignTextHistoryManager {
    private Map<Player, SignTextHistory> playerHistoryMap = new HashMap<>();
    private Provider<SignTextHistory> historyProvider;

    @Inject
    public SignTextHistoryManager(Provider<SignTextHistory> historyProvider) {
        this.historyProvider = historyProvider;
    }

    public void forgetPlayer(Player player) {
        playerHistoryMap.remove(player);
    }

    public SignTextHistory getHistory(Player player) {
        SignTextHistory history = playerHistoryMap.get(player);
        if (history == null) {
            history = historyProvider.get();
            playerHistoryMap.put(player, history);
        }
        return history;
    }
}
