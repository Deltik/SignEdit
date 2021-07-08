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

package org.deltik.mc.signedit.commands;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SignCommandTabCompleterTest {
    final int lineStartsAt = 1;

    private final SignCommandTabCompleter tabCompleter;
    private final Configuration config;
    private CommandSender commandSender;
    private Command command;
    private final String alias = "sign";
    private final Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> subcommandMap = new HashMap<>();

    private final String[] fancySignLines = new String[]{
            "§x§2§2§4§4§A§ADot",
            "§x§2§2§A§A§4§4Line",
            "§x§4§4§4§4§4§4Jason",
            "§x§F§F§F§F§8§8Sunny"
    };

    public SignCommandTabCompleterTest() {
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

    }

    @BeforeEach
    public void setUp() {
        Sign sign = createSign(fancySignLines);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(sign);
        when(sign.getBlock()).thenReturn(block);

        command = mock(Command.class);
        commandSender = mock(Player.class);
        when(commandSender.hasPermission(anyString())).thenReturn(true);
        when(((Player) commandSender).getTargetBlock(any(), anyInt())).thenReturn(block);
    }

    private static Sign createSign(String[] signLines) {
        Sign sign = mock(Sign.class);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(sign);
        when(sign.getBlock()).thenReturn(block);
        String[] signLinesCopy = signLines.clone();
        when(sign.getLines()).thenReturn(signLinesCopy);
        when(sign.getLine(anyInt())).then(invocation -> signLinesCopy[(int) invocation.getArgument(0)]);
        doAnswer(invocation ->
                signLinesCopy[(int) invocation.getArgument(0)] = invocation.getArgument(1)
        ).when(sign).setLine(anyInt(), anyString());
        when(sign.update()).thenReturn(true);
        return sign;
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
            assertTrue(result.contains(subcommand), subcommand + " is not in empty tab completion");
        }

        for (int line = config.getMinLine(); line <= config.getMaxLine(); line++) {
            assertTrue(
                    result.contains(String.valueOf(line)),
                    "Line " + line + " not offered for empty tab completion");
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
        BlockState dummyBlockState = mock(BlockState.class);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(dummyBlockState);
        when(((Player) commandSender).getTargetBlock(any(), anyInt())).thenReturn(block);

        List<String> result = tabComplete("2 ");
        assertEquals(0, result.size());

        result = tabComplete(" ");
        assertEquals(0, result.size());
    }

    @Test
    public void completeSignSubcommandWithoutLineSelectorSkipsLineSelectorHint() {
        Set<String> subcommandsWithoutLineSelector = Sets.newSet(
                "help",
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

    @Test
    public void signSetCompleteExistingSignLines() {
        List<String> result;

        result = tabComplete("set 1 ");
        assertTrue(result.contains("&#2244AADot"));
        assertEquals(1, result.size());

        result = tabComplete("2");
        assertTrue(result.contains("2-"));
        assertTrue(result.contains("2,"));
        assertEquals(2, result.size());

        result = tabComplete("2 ");
        assertTrue(result.contains("&#22AA44Line"));
        assertEquals(1, result.size());

        result = tabComplete("4,2-3,1 ");
        assertTrue(result.contains("&#2244AADot"));
        assertTrue(result.contains("&#22AA44Line"));
        assertTrue(result.contains("&#444444Jason"));
        assertTrue(result.contains("&#FFFF88Sunny"));
        assertEquals(4, result.size());
    }

    @Test
    public void signSetCompleteExistingSignLinesPartiallyFilled() {
        List<String> result;

        result = tabComplete("set 1-4 &#22");
        assertTrue(result.contains("&#2244AADot"));
        assertTrue(result.contains("&#22AA44Line"));
        assertEquals(2, result.size());

        result = tabComplete("1-4 other");
        assertEquals(0, result.size());
    }

    @Test
    public void signSetNoCompletionIfNotLookingAtSign() {
        BlockState dummyBlockState = mock(BlockState.class);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(dummyBlockState);
        when(((Player) commandSender).getTargetBlock(any(), anyInt())).thenReturn(block);

        List<String> result;

        result = tabComplete("2 ");
        assertEquals(0, result.size());

        result = tabComplete("1-4 ");
        assertEquals(0, result.size());
    }

    @Test
    public void signSetNoCompletionOutsideOfSubcommand() {
        List<String> result;

        result = tabComplete("clear 1-4 ");
        assertTrue(result.isEmpty());

        result = tabComplete("");
        assertFalse(result.contains("&#2244AADot"));

        result = tabComplete("4-2 ");
        assertTrue(result.isEmpty());
    }

    @Test
    public void noCompletionWithNoPermissions() {
        when(commandSender.hasPermission(anyString())).thenReturn(false);

        List<String> result = tabComplete("");

        assertEquals(0, result.size());
    }

    @Test
    public void noLineSelectorCompletionWithoutLineSelectorPermissions() {
        when(commandSender.hasPermission("signedit.use")).thenReturn(false);
        SignCommandTabCompleter.subcommandsWithLineSelector.forEach(
                subcommandName -> when(commandSender.hasPermission(
                        "signedit." + SignCommand.COMMAND_NAME + "." + subcommandName
                )).thenReturn(false)
        );

        List<String> results = tabComplete("");

        assertFalse(results.contains("1"));
        assertFalse(results.contains("2"));
        assertFalse(results.contains("3"));
        assertFalse(results.contains("4"));
    }
}
