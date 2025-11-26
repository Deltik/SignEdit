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
import net.deltik.mc.signedit.interactions.InteractionCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HelpSignSubcommandTest {
    Plugin plugin;
    ChatComms comms;
    Configuration config;
    Player player;
    SignEditPluginServices services;
    ChatCommsFactory chatCommsFactory;

    @BeforeEach
    public void setUp() throws InvalidDescriptionException {
        InputStream pluginYmlStream = getClass().getResourceAsStream("/plugin.yml");
        PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(
                pluginYmlStream
        );
        plugin = mock(SignEditPlugin.class);
        when(plugin.getDescription()).thenReturn(pluginDescriptionFile);

        player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(true);
        config = mock(Configuration.class);
        when(config.getLocale()).thenReturn(new Locale("en"));
        comms = new ChatComms(player, config);

        // Create mock services
        chatCommsFactory = mock(ChatCommsFactory.class);
        when(chatCommsFactory.create(any(Player.class))).thenReturn(comms);

        services = mock(SignEditPluginServices.class);
        when(services.plugin()).thenReturn(plugin);
        when(services.config()).thenReturn(config);
        when(services.chatCommsFactory()).thenReturn(chatCommsFactory);
    }

    private ArgParser argParse(String rawArgs) {
        return argParse(rawArgs.split(" "));
    }

    private ArgParser argParse(String[] rawArgs) {
        Set<String> subcommandNames = GeneratedSubcommandClasses.getSubcommandNames();
        return new ArgParser(config, rawArgs, subcommandNames);
    }

    private void spyComms() {
        comms = spy(new ChatComms(player, config));
        when(chatCommsFactory.create(any(Player.class))).thenReturn(comms);
    }

    private HelpSignSubcommand help(String args) {
        return help(argParse("help " + args));
    }

    private HelpSignSubcommand help(ArgParser argParser) {
        Set<String> subcommandNames = GeneratedSubcommandClasses.getSubcommandNames();
        BiFunction<String, SubcommandContext, InteractionCommand> subcommandFactory = (name, ctx) -> {
            // Capture permission result before stubbing to avoid Mockito confusion
            boolean hasPermission = player.hasPermission("signedit.sign." + name);
            InteractionCommand mockCmd = mock(InteractionCommand.class);
            when(mockCmd.isPermitted()).thenReturn(hasPermission);
            return mockCmd;
        };
        SubcommandContext context = new SubcommandContext(
                player, argParser.getArgs(), services, subcommandNames, subcommandFactory);
        return new HelpSignSubcommand(context);
    }

    @Test
    public void signHelpHeading() {
        String expected = "-----";

        InteractionCommand subcommand = help("");
        subcommand.execute();

        verify(player).sendMessage(contains(expected));
    }

    @Test
    public void signHelpForbiddenIfNoAvailableCommands() {
        when(player.hasPermission(anyString())).thenReturn(false);

        help("").execute();

        verify(player).sendMessage(contains("You are not allowed to use"));
    }

    @Test
    public void signHelpOffersOnlineDocumentation() {
        help("").execute();

        verify(player).sendMessage(contains("Online Help"));
    }

    @Test
    public void signHelpOffersOnlineDocumentationOnPage2() {
        help("2").execute();

        verify(player).sendMessage(contains("Online Help"));
    }

    @Test
    public void signHelpExtraLineWithOnlineDocumentationDisabled() {
        comms = spy(comms);
        when(chatCommsFactory.create(any(Player.class))).thenReturn(comms);
        help("").execute();
        verify(comms, times(HelpSignSubcommand.MAX_LINES - 2))
                .t(eq("print_subcommand_usage"), any());

        spyComms();
        doReturn("").when(comms).t(eq("online_documentation"), any());
        help("").execute();
        verify(comms, times(HelpSignSubcommand.MAX_LINES - 1))
                .t(eq("print_subcommand_usage"), any());
    }

    @Test
    public void signHelpPageTotal() {
        int commandCount, expected;

        commandCount = 12;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 2) + 1;
        signHelpPageTotal(commandCount, expected);

        commandCount = 100;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 2) + 1;
        signHelpPageTotal(commandCount, expected);

        commandCount = 500;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 2) + 1;
        signHelpPageTotal(commandCount, expected);
    }

    private void signHelpPageTotal(int commandCount, int expected) {
        spyComms();
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(commandCount, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms).t(eq("usage_page_numbering"), eq(1), eq(expected));
    }

    @Test
    public void signHelpPageTotalWithoutOnlineDocumentation() {
        int commandCount, expected;

        commandCount = 12;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 1) + 1;
        signHelpPageTotalWithoutOnlineDocumentation(commandCount, expected);

        commandCount = 100;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 1) + 1;
        signHelpPageTotalWithoutOnlineDocumentation(commandCount, expected);

        commandCount = 500;
        expected = (commandCount - 1) / (HelpSignSubcommand.MAX_LINES - 1) + 1;
        signHelpPageTotalWithoutOnlineDocumentation(commandCount, expected);
    }

    private void signHelpPageTotalWithoutOnlineDocumentation(int commandCount, int expected) {
        spyComms();
        doReturn("").when(comms).t(eq("online_documentation"), any());
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(commandCount, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms).t(eq("usage_page_numbering"), eq(1), eq(expected));
    }

    @Test
    public void signHelpDoesNotPaginateIfCommandsFitOnOnePage() {
        spyComms();
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(3, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms, never()).t(eq("usage_page_numbering"), any(), any());
    }

    @Test
    public void signHelpPaginationPage1() {
        spyComms();
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        List<String[]> items = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            items.add(new String[]{"sign", "sub" + i});
        }
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub1"), any());
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub8"), any());
        verify(comms, never()).t(eq("print_subcommand_usage"), eq("sign"), eq("sub9"), any());
    }

    @Test
    public void signHelpPaginationPage2() {
        spyComms();
        HelpSignSubcommand subcommand = help("2");
        subcommand = spy(subcommand);
        List<String[]> items = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            items.add(new String[]{"sign", "sub" + i});
        }
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms, never()).t(eq("print_subcommand_usage"), eq("sign"), eq("sub1"), any());
        verify(comms, never()).t(eq("print_subcommand_usage"), eq("sign"), eq("sub8"), any());
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub9"), any());
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub16"), any());
    }

    @Test
    public void signHelpPaginationPage2WithoutOnlineDocumentation() {
        spyComms();
        doReturn("").when(comms).t(eq("online_documentation"), any());
        HelpSignSubcommand subcommand = help("2");
        subcommand = spy(subcommand);
        List<String[]> items = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            items.add(new String[]{"sign", "sub" + i});
        }
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms, never()).t(eq("print_subcommand_usage"), eq("sign"), eq("sub1"), any());
        verify(comms, never()).t(eq("print_subcommand_usage"), eq("sign"), eq("sub9"), any());
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub10"), any());
        verify(comms, times(1)).t(eq("print_subcommand_usage"), eq("sign"), eq("sub18"), any());
    }

    @Test
    public void signHelpCurrentPageNeverLessThan1() {
        spyComms();
        HelpSignSubcommand subcommand = help("0");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(100, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms).t(eq("usage_page_numbering"), eq(1), eq(13));
    }

    @Test
    public void signHelpCurrentPageNeverGreaterThanMax() {
        spyComms();
        HelpSignSubcommand subcommand = help("31");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(100, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms).t(eq("usage_page_numbering"), eq(13), eq(13));
    }

    @Test
    public void signHelpInvalidPageGoesToPage1() {
        spyComms();
        HelpSignSubcommand subcommand = help("garbage");
        subcommand = spy(subcommand);
        List<String[]> items = Collections.nCopies(100, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        subcommand.execute();
        verify(comms).t(eq("usage_page_numbering"), eq(1), eq(13));
    }

    @Test
    public void tabCompletionReturnsPageNumbers() {
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        // 100 commands with 8 lines per page (MAX_LINES - 2) = 13 pages
        List<String[]> items = Collections.nCopies(100, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        List<String> completions = subcommand.getTabCompletions(argParse("help"));

        assertEquals(13, completions.size());
        for (int i = 1; i <= 13; i++) {
            assertTrue(completions.contains(String.valueOf(i)));
        }
        assertFalse(completions.contains("14"));
    }

    @Test
    public void tabCompletionReturnsEmptyWhenOnePage() {
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        // 3 commands fits on one page; no pagination needed
        List<String[]> items = Collections.nCopies(3, "foo".split(" "));
        doReturn(items).when(subcommand).getAllowedCommands();

        List<String> completions = subcommand.getTabCompletions(argParse("help"));

        // No page number completion when everything fits on one page
        assertEquals(0, completions.size());
    }

    @Test
    public void tabCompletionReturnsEmptyWhenNoCommands() {
        HelpSignSubcommand subcommand = help("");
        subcommand = spy(subcommand);
        doReturn(Collections.emptyList()).when(subcommand).getAllowedCommands();

        List<String> completions = subcommand.getTabCompletions(argParse("help"));

        // No page number completion when there are no commands
        assertEquals(0, completions.size());
    }
}