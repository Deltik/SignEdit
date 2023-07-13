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

import net.deltik.mc.signedit.exceptions.SignTextHistoryStackBoundsException;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class SignTextHistory {
    private final Player player;
    private final LinkedList<SignText> history = new LinkedList<>();
    int tailPosition = 0;

    public SignTextHistory(Player player) {
        this.player = player;
    }

    public void push(SignText signText) {
        while (history.size() > tailPosition) {
            history.removeLast();
        }
        history.addLast(signText);
        tailPosition = history.size();
    }

    public int undosRemaining() {
        return tailPosition;
    }

    public int redosRemaining() {
        return history.size() - tailPosition;
    }

    public SignText undo() {
        if (tailPosition <= 0) {
            throw new SignTextHistoryStackBoundsException("nothing_to_undo");
        }
        SignText previousSignText = history.get(tailPosition - 1);
        previousSignText.revertSign(player);
        tailPosition--;
        return previousSignText;
    }

    public SignText redo() {
        if (tailPosition == history.size()) {
            throw new SignTextHistoryStackBoundsException("nothing_to_redo");
        }
        SignText nextSignText = history.get(tailPosition);
        nextSignText.applySign(player);
        tailPosition++;
        return nextSignText;
    }
}
