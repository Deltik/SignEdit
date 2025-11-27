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
import net.deltik.mc.signedit.integrations.NoopSignEditValidator;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.SubcommandContext;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CutSignEditInteractionTest {
    private Configuration config;
    private Player player;
    private SignTextClipboardManager clipboardManager;
    private SignTextHistoryManager historyManager;
    private ChatCommsFactory chatCommsFactory;
    private ChatComms comms;
    private SignEditPluginServices services;

    private final String[] defaultSignLines = new String[]{"line1", "line2", "line3", "line4"};

    @BeforeEach
    public void setUp() {
        config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(1);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        when(config.getLocale()).thenReturn(new Locale("en"));

        player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(true);

        comms = spy(new ChatComms(player, config));
        chatCommsFactory = mock(ChatCommsFactory.class);
        when(chatCommsFactory.create(any(Player.class))).thenReturn(comms);

        clipboardManager = new SignTextClipboardManager();
        historyManager = new SignTextHistoryManager();

        services = mock(SignEditPluginServices.class);
        when(services.config()).thenReturn(config);
        when(services.chatCommsFactory()).thenReturn(chatCommsFactory);
        when(services.clipboardManager()).thenReturn(clipboardManager);
        when(services.historyManager()).thenReturn(historyManager);
    }

    private Sign createSign(String[] signLines) {
        Sign sign = mock(Sign.class);
        Block block = mock(Block.class);
        BlockData blockData = mock(org.bukkit.block.data.type.Sign.class);
        when(block.getState()).thenReturn(sign);
        when(sign.getBlock()).thenReturn(block);
        when(sign.getBlockData()).thenReturn(blockData);
        when(block.getRelative(any())).thenReturn(mock(Block.class));
        String[] signLinesCopy = signLines.clone();
        when(sign.getLines()).thenReturn(signLinesCopy);
        when(sign.getLine(anyInt())).then(
                (Answer<String>) invocation -> signLinesCopy[(int) invocation.getArgument(0)]
        );
        doAnswer(invocation ->
                signLinesCopy[(int) invocation.getArgument(0)] = invocation.getArgument(1)
        ).when(sign).setLine(anyInt(), anyString());
        when(sign.isPlaced()).thenReturn(true);
        return sign;
    }

    private ArgParser createArgParser(int[] selectedLines) {
        ArgParser argParser = mock(ArgParser.class);
        when(argParser.getLinesSelection()).thenReturn(selectedLines);
        return argParser;
    }

    private SubcommandContext createContext(SignText signText, ArgParser argParser, SignEditValidator validator) {
        when(services.signEditValidator()).thenReturn(validator);
        SubcommandContext context = new SubcommandContext(player, new String[0], services, null, null);
        // Use reflection to set the argParser and signText since they're normally lazy-initialized
        try {
            java.lang.reflect.Field argParserField = SubcommandContext.class.getDeclaredField("argParser");
            argParserField.setAccessible(true);
            argParserField.set(context, argParser);

            java.lang.reflect.Field signTextField = SubcommandContext.class.getDeclaredField("signText");
            signTextField.setAccessible(true);
            signTextField.set(context, signText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return context;
    }

    @Test
    public void cutAllLinesShowsClipboardDumpOnly() {
        // Setup: normal cut with NoopValidator (no external modification)
        Sign sign = createSign(defaultSignLines.clone());
        SignShim signShim = new SignShim(sign);
        SignEditValidator validator = new NoopSignEditValidator();
        SignText signText = new SignText(validator);
        ArgParser argParser = createArgParser(new int[]{0, 1, 2, 3});

        SubcommandContext context = createContext(signText, argParser, validator);
        CutSignEditInteraction interaction = new CutSignEditInteraction(context);

        // Execute the cut
        interaction.interact(player, signShim, SideShim.FRONT);

        // Verify clipboard dump is shown
        verify(comms).tell(comms.t("lines_cut_section"));
        verify(comms).dumpLines(any());

        // Verify NO sign result section is shown (no external modification)
        verify(comms, never()).tell(contains("after_section"));
        verify(comms, never()).tell(argThat(arg ->
                arg != null && arg.contains("Modified by another plugin")));
    }

    @Test
    public void cutWithExternalModificationShowsSignResultSection() {
        // Setup: validator that restores line 1 when we try to clear it
        SignEditValidator modifyingValidator = new SignEditValidator() {
            @Override
            public String[] validate(SignShim proposedSign, SideShim side, Player player) {
                String[] lines = proposedSign.getSide(side).getLines().clone();
                // Simulate another plugin keeping line 1 non-empty
                if (lines[1].isEmpty()) {
                    lines[1] = "KEPT_BY_PLUGIN";
                }
                return lines;
            }

            @Override
            public void validate(SignChangeEvent signChangeEvent) {
            }
        };

        Sign sign = createSign(defaultSignLines.clone());
        SignShim signShim = new SignShim(sign);
        SignText signText = new SignText(modifyingValidator);
        ArgParser argParser = createArgParser(new int[]{0, 1, 2, 3});

        SubcommandContext context = createContext(signText, argParser, modifyingValidator);
        CutSignEditInteraction interaction = new CutSignEditInteraction(context);

        // Execute the cut
        interaction.interact(player, signShim, SideShim.FRONT);

        // Verify clipboard dump is shown first
        verify(comms).tell(comms.t("lines_cut_section"));
        verify(comms).dumpLines(any());

        // Verify sign result section IS shown (external modification occurred)
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(comms, atLeast(2)).tell(messageCaptor.capture());

        // Check that one of the messages contains the external modification notice
        boolean hasExternalModMessage = messageCaptor.getAllValues().stream()
                .anyMatch(msg -> msg != null && msg.contains("Modified by another plugin"));
        assertTrue(hasExternalModMessage, "Expected external modification message to be shown");

        // Verify the modified line is shown
        boolean hasModifiedLine = messageCaptor.getAllValues().stream()
                .anyMatch(msg -> msg != null && msg.contains("KEPT_BY_PLUGIN"));
        assertTrue(hasModifiedLine, "Expected modified line content to be shown");
    }

    @Test
    public void cutPartialLinesWithExternalModificationShowsOnlyAffectedLines() {
        // Setup: validator that modifies line 2 when we try to clear it
        SignEditValidator modifyingValidator = new SignEditValidator() {
            @Override
            public String[] validate(SignShim proposedSign, SideShim side, Player player) {
                String[] lines = proposedSign.getSide(side).getLines().clone();
                // Simulate another plugin modifying line 2 to something else
                if (lines[2].isEmpty()) {
                    lines[2] = "MODIFIED_LINE_3";
                }
                return lines;
            }

            @Override
            public void validate(SignChangeEvent signChangeEvent) {
            }
        };

        Sign sign = createSign(defaultSignLines.clone());
        SignShim signShim = new SignShim(sign);
        SignText signText = new SignText(modifyingValidator);
        // Only cut lines 1, 2, 3 (0-indexed: 0, 1, 2)
        ArgParser argParser = createArgParser(new int[]{0, 1, 2});

        SubcommandContext context = createContext(signText, argParser, modifyingValidator);
        CutSignEditInteraction interaction = new CutSignEditInteraction(context);

        // Execute the cut
        interaction.interact(player, signShim, SideShim.FRONT);

        // Verify clipboard dump is shown
        verify(comms).tell(comms.t("lines_cut_section"));
        verify(comms).dumpLines(any());

        // Capture all messages
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(comms, atLeast(2)).tell(messageCaptor.capture());

        // Verify external modification message is shown
        boolean hasExternalModMessage = messageCaptor.getAllValues().stream()
                .anyMatch(msg -> msg != null && msg.contains("Modified by another plugin"));
        assertTrue(hasExternalModMessage, "Expected external modification message");

        // Verify the modified line content is shown
        boolean hasModifiedContent = messageCaptor.getAllValues().stream()
                .anyMatch(msg -> msg != null && msg.contains("MODIFIED_LINE_3"));
        assertTrue(hasModifiedContent, "Expected modified line content to be shown");
    }

    @Test
    public void cutCopiesOriginalLinesToClipboard() {
        Sign sign = createSign(defaultSignLines.clone());
        SignShim signShim = new SignShim(sign);
        SignEditValidator validator = new NoopSignEditValidator();
        SignText signText = new SignText(validator);
        ArgParser argParser = createArgParser(new int[]{0, 1, 2, 3});

        SubcommandContext context = createContext(signText, argParser, validator);
        CutSignEditInteraction interaction = new CutSignEditInteraction(context);

        // Execute the cut
        interaction.interact(player, signShim, SideShim.FRONT);

        // Verify clipboard contains original lines
        SignText clipboard = clipboardManager.getClipboard(player);
        assertNotNull(clipboard);
        assertArrayEquals(defaultSignLines, clipboard.getLines());
    }

    @Test
    public void cutWithNoExternalModificationClearsSignLines() {
        Sign sign = createSign(defaultSignLines.clone());
        SignShim signShim = new SignShim(sign);
        SignEditValidator validator = new NoopSignEditValidator();
        SignText signText = new SignText(validator);
        ArgParser argParser = createArgParser(new int[]{0, 1, 2, 3});

        SubcommandContext context = createContext(signText, argParser, validator);
        CutSignEditInteraction interaction = new CutSignEditInteraction(context);

        // Execute the cut
        interaction.interact(player, signShim, SideShim.FRONT);

        // Verify all lines were cleared on the sign
        verify(sign).setLine(0, "");
        verify(sign).setLine(1, "");
        verify(sign).setLine(2, "");
        verify(sign).setLine(3, "");
    }
}