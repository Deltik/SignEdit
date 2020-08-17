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
import org.bukkit.plugin.Plugin;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.DaggerSignEditPluginComponent;
import org.deltik.mc.signedit.SignEditPluginComponent;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Singleton
public class SignCommandTabCompleterTest {
    final int lineStartsAt = 1;

    private final SignCommandTabCompleter tabCompleter;
    private final Configuration config;
    private final CommandSender commandSender;
    private final Command command;
    private final String alias = "sign";
    private final Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> subcommandMap = new HashMap<>();

    public SignCommandTabCompleterTest() {
        SignEditPluginComponent test = DaggerSignEditPluginComponent.builder().plugin(mock(Plugin.class)).build();
        config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(lineStartsAt);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        subcommandMap.put("help", null);
        subcommandMap.put("ui", null);
        subcommandMap.put("set", null);
        subcommandMap.put("clear", null);
        subcommandMap.put("cancel", null);
        subcommandMap.put("status", null);
        subcommandMap.put("copy", null);
        subcommandMap.put("cut", null);
        subcommandMap.put("paste", null);
        subcommandMap.put("undo", null);
        subcommandMap.put("redo", null);
        subcommandMap.put("version", null);
        this.tabCompleter = new SignCommandTabCompleter(subcommandMap, config);

        commandSender = mock(CommandSender.class);
        command = mock(Command.class);
    }

    private List<String> tabComplete(String args) {
        String[] argsSplit = args.split(" ", -1);
        if (args.equals("")) argsSplit = new String[]{""};
        return tabCompleter.onTabComplete(commandSender, command, alias, argsSplit);
    }

    @Test
    public void signSubcommandsAll() {
        List<String> result = tabComplete("");

        for (String subcommand : subcommandMap.keySet()) {
            assertTrue(subcommand + " is not in empty tab completion", result.contains(subcommand));
        }

        for (int line = config.getMinLine(); line <= config.getMaxLine(); line++) {
            assertTrue(
                    "Line " + line + " not offered for empty tab completion",
                    result.contains(String.valueOf(line))
            );
        }

        assertEquals(subcommandMap.size() + (config.getMaxLine() - config.getMinLine() + 1), result.size());
    }

    @Test
    public void signSubcommandsStartingWith() {
        List<String> result = tabComplete("c");

        assertTrue(result.contains("cancel"));
        assertTrue(result.contains("clear"));
        assertTrue(result.contains("copy"));
        assertTrue(result.contains("cut"));
        assertFalse(result.contains("paste"));
        assertFalse(result.contains("1"));
    }

    @Test
    public void signSubcommandsLineSelector() {
        List<String> result = tabComplete("set 3");

        assertTrue(result.contains("3-"));
        assertTrue(result.contains("3,"));
        assertEquals(2, result.size());
    }

    @Test
    public void completeSignSubcommandShowsLineSelectorHint() {
        Set<String> subcommandsWithLineSelector = Sets.newSet("set", "clear", "copy", "cut");
        for (String subcommand : subcommandsWithLineSelector) {
            List<String> result = tabComplete(subcommand + " ");

            assertTrue(result.contains("1"));
            assertTrue(result.contains("2"));
            assertTrue(result.contains("3"));
            assertTrue(result.contains("4"));
            assertEquals(4, result.size());
        }
    }

    @Test
    public void completeSignSubcommandWithoutSpaceSkipsLineSelectorHint() {
        List<String> result = tabComplete("set");

        assertTrue(result.contains("set"));
        assertEquals(1, result.size());
    }

    @Test
    public void signSubcommandLineSelectorShorthandNoDuplicateHint() {
        List<String> result = tabComplete("2 ");

        assertEquals(0, result.size());

        result = tabComplete(" ");
        assertEquals(0, result.size());
    }

    @Test
    public void completeSignSubcommandWithoutLineSelectorSkipsLineSelectorHint() {
        Set<String> subcommandsWithoutLineSelector = Sets.newSet(
                "ui",
                "cancel",
                "status",
                "paste",
                "undo",
                "redo",
                "version"
        );
        for (String subcommand : subcommandsWithoutLineSelector) {
            List<String> result = tabComplete(subcommand + " ");

            assertEquals(0, result.size());
        }
    }

    @Test
    public void signSubcommandLineSelectorAvailableLines() {
        List<String> result;

        result = tabComplete("set 2-");
        assertFalse(result.contains("2-1"));
        assertFalse(result.contains("2-2"));
        assertTrue(result.contains("2-3"));
        assertTrue(result.contains("2-4"));
        assertEquals(2, result.size());

        result = tabComplete("1,");
        assertTrue(result.contains("1,2"));
        assertTrue(result.contains("1,3"));
        assertTrue(result.contains("1,4"));
        assertEquals(3, result.size());

        result = tabComplete("set 2,");
        assertTrue(result.contains("2,1"));
        assertFalse(result.contains("2,2"));
        assertTrue(result.contains("2,3"));
        assertTrue(result.contains("2,4"));
        assertEquals(3, result.size());

        result = tabComplete("set 4,1-");
        assertTrue(result.contains("4,1-2"));
        assertTrue(result.contains("4,1-3"));
        assertEquals(2, result.size());

        result = tabComplete("set 1-2,3,3,3,");
        assertTrue(result.contains("1-2,3,3,3,4"));
        assertEquals(1, result.size());

        result = tabComplete("set 1-2,3-3,3-");
        assertTrue(result.contains("1-2,3-3,3-4"));
        assertEquals(1, result.size());
    }

    @Test
    public void signSubcommandsLineSelectorNoMore() {
        List<String> result = tabComplete("set 1,2,3,4");
        assertTrue(result.isEmpty());

        result = tabComplete("set 1-4");
        assertTrue(result.isEmpty());

        result = tabComplete("clear 4,1,2-3");
        assertTrue(result.isEmpty());
    }

    @Test
    public void signSubcommandsLineSelectorIgnoreInvalid() {
        List<String> result;

        result = tabComplete("cut 4-3,");
        assertTrue(result.isEmpty());

        result = tabComplete("cut 1-2-");
        assertTrue(result.isEmpty());

        result = tabComplete("cut 1-2-3,");
        assertTrue(result.isEmpty());

        result = tabComplete("copy 1-2");
        assertTrue(result.contains("1-2,"));
        assertFalse(result.contains("1-2-"));
        assertEquals(1, result.size());

        result = tabComplete("set 1,2,4");
        assertTrue(result.contains("1,2,4,"));
        assertFalse(result.contains("1,2,4-"));
        assertEquals(1, result.size());

        result = tabComplete("clear 3-4,2");
        assertTrue(result.contains("3-4,2,"));
        assertFalse(result.contains("3-4,2-"));
        assertEquals(1, result.size());

        result = tabComplete("clear 5");
        assertEquals(0, result.size());
    }
}
