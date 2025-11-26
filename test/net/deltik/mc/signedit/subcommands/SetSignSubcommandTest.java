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

import net.deltik.mc.signedit.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SetSignSubcommandTest {
    private Configuration config;
    private Player player;
    private SignEditPluginServices services;

    private final String[] fancySignLines = new String[]{
            "§x§2§2§4§4§A§ADot",
            "§x§2§2§A§A§4§4Line",
            "§x§4§4§4§4§4§4Jason",
            "§x§F§F§F§F§8§8Sunny"
    };

    @BeforeEach
    public void setUp() {
        config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(1);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        when(config.getLocale()).thenReturn(new Locale("en"));

        player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(true);

        ChatComms comms = new ChatComms(player, config);
        ChatCommsFactory chatCommsFactory = mock(ChatCommsFactory.class);
        when(chatCommsFactory.create(any(Player.class))).thenReturn(comms);

        services = mock(SignEditPluginServices.class);
        when(services.config()).thenReturn(config);
        when(services.chatCommsFactory()).thenReturn(chatCommsFactory);
    }

    private Sign createSign(String[] signLines) {
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

    private void setupPlayerLookingAtSign(Sign sign) {
        Block block = sign.getBlock();
        when(player.getTargetBlock(any(), anyInt())).thenReturn(block);
        when(player.getEyeLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
    }

    private void setupPlayerNotLookingAtSign() {
        BlockState dummyBlockState = mock(BlockState.class);
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(dummyBlockState);
        when(player.getTargetBlock(any(), anyInt())).thenReturn(block);
        when(player.getEyeLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));
    }

    private ArgParser argParse(String rawArgs) {
        Set<String> subcommandNames = GeneratedSubcommandClasses.getSubcommandNames();
        return new ArgParser(config, rawArgs.split(" "), subcommandNames);
    }

    private SetSignSubcommand createSubcommand(String args) {
        ArgParser argParser = argParse(args);
        Set<String> subcommandNames = GeneratedSubcommandClasses.getSubcommandNames();
        SubcommandContext context = new SubcommandContext(
                player, argParser.getArgs(), services, subcommandNames, null);
        return new SetSignSubcommand(context);
    }

    @Test
    public void tabCompletionReturnsExistingSignLines() {
        Sign sign = createSign(fancySignLines);
        setupPlayerLookingAtSign(sign);

        SetSignSubcommand subcommand = createSubcommand("set 1 ");
        List<String> completions = subcommand.getTabCompletions(argParse("set 1 "));

        assertTrue(completions.contains("&#2244AADot"));
        assertEquals(1, completions.size());
    }

    @Test
    public void tabCompletionReturnsMultipleLines() {
        Sign sign = createSign(fancySignLines);
        setupPlayerLookingAtSign(sign);

        SetSignSubcommand subcommand = createSubcommand("set 4,2-3,1 ");
        List<String> completions = subcommand.getTabCompletions(argParse("set 4,2-3,1 "));

        assertTrue(completions.contains("&#2244AADot"));
        assertTrue(completions.contains("&#22AA44Line"));
        assertTrue(completions.contains("&#444444Jason"));
        assertTrue(completions.contains("&#FFFF88Sunny"));
        assertEquals(4, completions.size());
    }

    @Test
    public void tabCompletionFiltersWithPrefix() {
        Sign sign = createSign(fancySignLines);
        setupPlayerLookingAtSign(sign);

        SetSignSubcommand subcommand = createSubcommand("set 1-4 &#22");
        List<String> completions = subcommand.getTabCompletions(argParse("set 1-4 &#22"));

        assertTrue(completions.contains("&#2244AADot"));
        assertTrue(completions.contains("&#22AA44Line"));
        assertEquals(2, completions.size());
    }

    @Test
    public void tabCompletionReturnsEmptyWhenNoMatch() {
        Sign sign = createSign(fancySignLines);
        setupPlayerLookingAtSign(sign);

        SetSignSubcommand subcommand = createSubcommand("set 1-4 other");
        List<String> completions = subcommand.getTabCompletions(argParse("set 1-4 other"));

        assertEquals(0, completions.size());
    }

    @Test
    public void tabCompletionReturnsEmptyWhenNotLookingAtSign() {
        setupPlayerNotLookingAtSign();

        SetSignSubcommand subcommand = createSubcommand("set 1 ");
        List<String> completions = subcommand.getTabCompletions(argParse("set 1 "));

        assertEquals(0, completions.size());
    }

    @Test
    public void tabCompletionForLineSelectorShorthand() {
        Sign sign = createSign(fancySignLines);
        setupPlayerLookingAtSign(sign);

        SetSignSubcommand subcommand = createSubcommand("2 ");
        List<String> completions = subcommand.getTabCompletions(argParse("2 "));

        assertTrue(completions.contains("&#22AA44Line"));
        assertEquals(1, completions.size());
    }
}