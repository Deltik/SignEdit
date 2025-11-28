/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class SignEditInteraction {
    private final SubcommandContext context;

    protected SignEditInteraction(SubcommandContext context) {
        this.context = context;
    }

    protected SubcommandContext context() {
        return context;
    }

    // Convenience accessors for commonly used services
    protected SignText signText() {
        return context.signText();
    }

    protected ArgParser argParser() {
        return context.argParser();
    }

    protected ChatCommsFactory chatCommsFactory() {
        return context.services().chatCommsFactory();
    }

    protected SignTextHistoryManager historyManager() {
        return context.services().historyManager();
    }

    protected SignTextClipboardManager clipboardManager() {
        return context.services().clipboardManager();
    }

    protected SignEditInteractionManager interactionManager() {
        return context.services().interactionManager();
    }

    protected Configuration config() {
        return context.services().config();
    }

    protected Plugin plugin() {
        return context.services().plugin();
    }

    public abstract void interact(Player player, SignShim sign, SideShim side);

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getActionHint(ChatComms comms) {
        return comms.t("right_click_sign_to_apply_action_hint");
    }

    public void cleanup(Event event) {
    }

    public void cleanup() {
        cleanup(new Event() {
            @NotNull
            @Override
            public HandlerList getHandlers() {
                return new HandlerList();
            }
        });
    }
}
