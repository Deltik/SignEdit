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

package net.deltik.mc.signedit.commands;

import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.subcommands.GeneratedSubcommandClasses;
import net.deltik.mc.signedit.subcommands.SignSubcommand;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import net.deltik.mc.signedit.subcommands.SubcommandRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SignCommandTabCompleterTest {
    final int lineStartsAt = 1;

    private SignCommandTabCompleter tabCompleter;
    private Configuration config;
    private SubcommandRegistry registry;
    private CommandSender commandSender;
    private Command command;
    private SignSubcommand signSubcommand;
    private final String alias = "sign";
    private final Set<String> subcommandNames = GeneratedSubcommandClasses.getSubcommandNames();

    private final String[] fancySignLines = new String[]{
            "§x§2§2§4§4§A§ADot",
            "§x§2§2§A§A§4§4Line",
            "§x§4§4§4§4§4§4Jason",
            "§x§F§F§F§F§8§8Sunny"
    };

    @BeforeEach
    public void setUp() {
        config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(lineStartsAt);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        when(config.getLocale()).thenCallRealMethod();

        // Create mock registry
        registry = mock(SubcommandRegistry.class);
        when(registry.getSubcommandNames()).thenReturn(subcommandNames);
        when(registry.hasSubcommand(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return GeneratedSubcommandClasses.hasSubcommand(name);
        });
        when(registry.supportsLineSelector(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            return GeneratedSubcommandClasses.supportsLineSelector(name);
        });

        signSubcommand = mock(SignSubcommand.class);
        when(signSubcommand.isPermitted()).thenReturn(true);
        when(signSubcommand.getTabCompletions(any())).thenReturn(Collections.emptyList());

        when(registry.createContext(any(Player.class), any(String[].class))).thenAnswer(invocation -> {
            return mock(SubcommandContext.class);
        });
        when(registry.createSubcommand(anyString(), any(SubcommandContext.class))).thenReturn(signSubcommand);

        tabCompleter = spy(new SignCommandTabCompleter(registry, config));
        doReturn(signSubcommand).when(tabCompleter).getSignSubcommand(any(), any());

        Sign sign = createSign(fancySignLines);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(sign);
        when(sign.getBlock()).thenReturn(block);

        command = mock(Command.class);
        commandSender = mock(Player.class);
        when(commandSender.hasPermission(anyString())).thenReturn(true);
        when(((Player) commandSender).getTargetBlock(any(), anyInt())).thenReturn(block);
        when(((Player) commandSender).getEyeLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
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
        when(sign.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
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

        for (String subcommand : subcommandNames) {
            assertTrue(result.contains(subcommand), subcommand + " is not in empty tab completion");
        }

        for (int line = config.getMinLine(); line <= config.getMaxLine(); line++) {
            assertTrue(
                    result.contains(String.valueOf(line)),
                    "Line " + line + " not offered for empty tab completion");
        }

        assertEquals(subcommandNames.size() + (config.getMaxLine() - config.getMinLine() + 1), result.size());
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

            assertEquals(0, result.size(), "Subcommand " + subcommand + " failed test");
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
    public void signSubcommandsLineSelectorShorthandNoDuplicateLineSelectors() {
        List<String> result;

        result = tabComplete("0-5 ");
        assertEquals(0, result.size());
    }

    @Test
    public void noCompletionWithNoPermissions() {
        when(signSubcommand.isPermitted()).thenReturn(false);

        List<String> result = tabComplete("");

        assertEquals(0, result.size());
    }

    @Test
    public void noLineSelectorCompletionWithoutLineSelectorPermissions() {
        when(signSubcommand.isPermitted()).thenReturn(false);

        List<String> results = tabComplete("");

        assertFalse(results.contains("1"));
        assertFalse(results.contains("2"));
        assertFalse(results.contains("3"));
        assertFalse(results.contains("4"));
    }

    @Test
    public void subcommandArgsNotCompletedUntilSubcommandFullyTyped() {
        // Mock the subcommand to return page numbers (simulating HelpSignSubcommand with 2 pages)
        when(signSubcommand.getTabCompletions(any())).thenReturn(Arrays.asList("1", "2"));

        // Partial subcommand name should only complete the subcommand, not its args
        List<String> result = tabComplete("hel");
        assertTrue(result.contains("help"));
        assertFalse(result.contains("1"), "Page numbers should not appear before subcommand is fully typed");
        assertFalse(result.contains("2"), "Page numbers should not appear before subcommand is fully typed");

        // Full subcommand name without space should only complete the subcommand
        result = tabComplete("help");
        assertTrue(result.contains("help"));
        assertFalse(result.contains("1"), "Page numbers should not appear without trailing space");
        assertFalse(result.contains("2"), "Page numbers should not appear without trailing space");

        // Full subcommand name with space should show subcommand args
        result = tabComplete("help ");
        assertTrue(result.contains("1"), "Page numbers should appear after subcommand with trailing space");
        assertTrue(result.contains("2"), "Page numbers should appear after subcommand with trailing space");
    }
}