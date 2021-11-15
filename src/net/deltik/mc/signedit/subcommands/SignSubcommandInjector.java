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

package net.deltik.mc.signedit.subcommands;

import dagger.BindsInstance;
import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.ChatComms;
import org.bukkit.entity.Player;

public interface SignSubcommandInjector<T extends SignSubcommand> {
    T command();

    abstract class Builder<T extends SignSubcommand> {
        public abstract SignSubcommandInjector<T> build();

        @BindsInstance
        public abstract Builder<T> player(Player player);

        @BindsInstance
        public abstract Builder<T> argParser(ArgParser args);

        @BindsInstance
        public abstract Builder<T> comms(ChatComms comms);
    }
}
