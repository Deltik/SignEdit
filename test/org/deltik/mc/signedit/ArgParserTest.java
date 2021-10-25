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

package org.deltik.mc.signedit;

import org.deltik.mc.signedit.exceptions.NumberParseLineSelectionException;
import org.deltik.mc.signedit.exceptions.OutOfBoundsLineSelectionException;
import org.deltik.mc.signedit.exceptions.RangeOrderLineSelectionException;
import org.deltik.mc.signedit.exceptions.RangeParseLineSelectionException;
import org.deltik.mc.signedit.subcommands.SignSubcommandModule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArgParserTest {
    private String subcommand;
    private int[] selectedLines;
    Exception selectedLinesError;
    List<String> remainder;

    private void parse(String args) {
        parse(args, 1);
    }

    private void parse(String args, int lineStartsAt) {
        Configuration config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(lineStartsAt);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        Set<String> subcommandNames = SignSubcommandModule.provideSubcommandNames();

        String[] argsSplit = args.split(" ");
        if (args.equals("")) argsSplit = new String[]{};
        ArgParser argParser = new ArgParser(config, argsSplit, subcommandNames);

        subcommand = argParser.getSubcommand();
        selectedLines = argParser.getLinesSelection();
        selectedLinesError = argParser.getLinesSelectionError();
        remainder = argParser.getRemainder();
    }

    @Test
    public void parseSign() {
        parse("");

        assertEquals("help", subcommand);
        assertArrayEquals(new int[0], selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignGarbageGoesToHelp() {
        parse("garbage1 garbage2 garbage3");

        assertEquals("help", subcommand);
        assertArrayEquals(ArgParser.NO_LINES_SELECTED, selectedLines);
        assertEquals(Arrays.asList("garbage1", "garbage2", "garbage3"), remainder);
    }

    @Test
    public void parseSignHelpWithPageNumber() {
        parse("help 2");

        assertEquals("help", subcommand);
        assertArrayEquals(ArgParser.NO_LINES_SELECTED, selectedLines);
        assertEquals(Collections.singletonList("2"), remainder);
    }

    @Test
    public void parseSignUi() {
        parse("ui");

        assertEquals("ui", subcommand);
        assertArrayEquals(new int[0], selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignSetLineText() {
        parse("set 3 foo bar");

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{2}, selectedLines);
        assertEquals(Arrays.asList("foo", "bar"), remainder);
    }

    @Test
    public void parseSignSetLineTextWhenLineStartsAt0() {
        parse("set 3 foo bar", 0);

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{3}, selectedLines);
        assertEquals(Arrays.asList("foo", "bar"), remainder);
    }

    @Test
    public void parseSignSetLinesText() {
        parse("set 3,1 alpha bravo charlie");

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{0, 2}, selectedLines);
        assertEquals(Arrays.asList("alpha", "bravo", "charlie"), remainder);
    }

    @Test
    public void parseSignSetLinesTextWhenLineStartsAt0() {
        parse("set 3,1 alpha bravo charlie", 0);

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{1, 3}, selectedLines);
        assertEquals(Arrays.asList("alpha", "bravo", "charlie"), remainder);
    }

    @Test
    public void parseSignClearLine() {
        parse("clear 2");

        assertEquals("clear", subcommand);
        assertArrayEquals(new int[]{1}, selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignClearLines() {
        parse("clear 1,2,4");

        assertEquals("clear", subcommand);
        assertArrayEquals(new int[]{0, 1, 3}, selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignCancel() {
        parse("cancel");

        assertEquals("cancel", subcommand);
        assertArrayEquals(new int[0], selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignHelp() {
        parse("help");

        assertEquals("help", subcommand);
        assertArrayEquals(new int[0], selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignCopy() {
        parse("copy 1-2,4");

        assertEquals("copy", subcommand);
        assertArrayEquals(new int[]{0, 1, 3}, selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignSetLineAlias() {
        parse("4 a b c");

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{3}, selectedLines);
        assertEquals(Arrays.asList("a", "b", "c"), remainder);
    }

    @Test
    public void parseSignSetLinesAlias() {
        parse("1,3,2 =D");

        assertEquals("set", subcommand);
        assertArrayEquals(new int[]{0, 1, 2}, selectedLines);
        assertEquals(Collections.singletonList("=D"), remainder);
    }

    @Test
    public void parseSignClearLineAlias() {
        parse("1");

        assertEquals("clear", subcommand);
        assertArrayEquals(new int[]{0}, selectedLines);
        assertTrue(remainder.isEmpty());
    }

    @Test
    public void parseSignLinesSelectorDuplicates() {
        parse("set 2,2,2,3,3 anything");

        assertArrayEquals(new int[]{1, 2}, selectedLines);
    }

    @Test
    public void parseSignLinesSelectorOverlaps() {
        parse("set 1-3,2-4 excurgated");

        assertArrayEquals(new int[]{0, 1, 2, 3}, selectedLines);
    }

    @Test
    public void parseSignLinesFailsRangeLowerHigherThanHigher() {
        parse("set 3-1 #NOPE");

        assertTrue(selectedLinesError instanceof RangeOrderLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsRangeOutOfBounds() {
        parse("set 0-4 #NOPE");

        assertTrue(selectedLinesError instanceof OutOfBoundsLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsLowerBoundWhenLineStartsAt0() {
        parse("set 4 #NOPE", 0);

        assertTrue(selectedLinesError instanceof OutOfBoundsLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsLowerBoundWhenLineStartsAt1() {
        parse("set 0 #NOPE", 1);

        assertTrue(selectedLinesError instanceof OutOfBoundsLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsNegativeNumber() {
        parse("set -1 #NOPE");

        assertTrue(selectedLinesError instanceof OutOfBoundsLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsGarbageRange() {
        parse("set 1-3-4 #NOPE");

        assertTrue(selectedLinesError instanceof RangeParseLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsNotANumber() {
        parse("set forgot to put a line number");

        assertTrue(selectedLinesError instanceof NumberParseLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsEmptyDelimiter() {
        parse("set 1,,3");

        assertTrue(selectedLinesError instanceof NumberParseLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsEmptyDelimiterAtStart() {
        parse("set ,1,3");

        assertTrue(selectedLinesError instanceof NumberParseLineSelectionException);
    }

    @Test
    public void parseSignLinesFailsEmptyDelimiterAtEnd() {
        parse("set 1,3,");

        assertTrue(selectedLinesError instanceof NumberParseLineSelectionException);
    }
}