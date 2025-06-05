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

package net.deltik.mc.signedit.subcommands;

import dagger.BindsInstance;
import dagger.Subcomponent;
import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.ArgParserArgs;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import net.deltik.mc.signedit.interactions.SignEditInteractionModule;
import org.bukkit.entity.Player;

import javax.inject.Provider;
import java.util.Map;

@PerSubcommand
@Subcomponent(modules = {SignEditInteractionModule.class})
public interface SignSubcommandComponent {

    Map<String, Provider<InteractionCommand>> subcommandProviders();

    ArgParser argParser();

    @Subcomponent.Builder
    abstract class Builder {
        public abstract SignSubcommandComponent build();

        @BindsInstance
        public abstract Builder player(Player player);

        @BindsInstance
        public abstract Builder commandArgs(@ArgParserArgs String[] args);
    }
}
