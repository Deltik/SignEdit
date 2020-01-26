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

package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SignCommandTabCompleter implements TabCompleter {
    private Set<String> subcommandNames;

    @Inject
    public SignCommandTabCompleter(Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders) {
        subcommandNames = commandBuilders.keySet();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        Set<String> candidateSubcommands = subcommandNames;
        candidateSubcommands = candidateSubcommands
                .stream()
                .filter(
                        name -> name.startsWith(args[0])
                )
                .collect(Collectors.toSet());
        return new ArrayList<>(candidateSubcommands);
    }
}
