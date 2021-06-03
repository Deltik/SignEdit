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

import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import org.bukkit.entity.Player;

@Module(subcomponents = {
        ChatCommsModule.ChatCommsComponent.class
})
public abstract class ChatCommsModule {
    @Subcomponent
    public interface ChatCommsComponent {
        ChatComms comms();

        @Subcomponent.Builder
        abstract class Builder {
            public abstract ChatCommsComponent build();

            @BindsInstance
            public abstract Builder player(Player player);
        }
    }
}
