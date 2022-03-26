/*
 * Copyright (C) 2017-2022 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.exceptions.NumberParseLineSelectionException;
import net.deltik.mc.signedit.exceptions.OutOfBoundsLineSelectionException;
import net.deltik.mc.signedit.exceptions.RangeOrderLineSelectionException;
import net.deltik.mc.signedit.exceptions.RangeParseLineSelectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LineSelectorParserTest {
    final int lineStartsAt = 1;
    LineSelectorParser unit;

    @BeforeEach
    void setUp() {
        Configuration config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(lineStartsAt);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        unit = new LineSelectorParser(config);
    }

    @Test
    void isLineSelection() {
        assertTrue(unit.isLineSelection("1"));
        assertTrue(unit.isLineSelection("1-4"));
        assertTrue(unit.isLineSelection("2,4"));
        assertTrue(unit.isLineSelection("1-4,1,2,2,3,3,3,4,4,4,4"));

        assertFalse(unit.isLineSelection(""));
        assertFalse(unit.isLineSelection("-1"));
        assertFalse(unit.isLineSelection("35"));
        assertFalse(unit.isLineSelection("2-1"));
        assertFalse(unit.isLineSelection("0"));
        assertFalse(unit.isLineSelection("1-4,"));
    }

    private void assertElementsMatch(List<?> expected, List<?> actual) {
        assertEquals(expected.size(), actual.size(), "Expected " + expected.size() + " elements but got " + actual.size());
        for (Object element : expected) {
            assertTrue(actual.contains(element), "Actual should have contained " + element);
        }
    }

    @Test
    void suggestCompletionFromScratch() {
        List<String> completions = unit.suggestCompletion("");
        List<String> expected = Arrays.asList("1", "2", "3", "4");
        assertElementsMatch(expected, completions);
    }

    @Test
    void suggestCompletionFromScratchLineStartsAt0() {
        Configuration config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(0);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        unit = new LineSelectorParser(config);

        List<String> completions = unit.suggestCompletion("");
        List<String> expected = Arrays.asList("0", "1", "2", "3");
        assertElementsMatch(expected, completions);
    }

    @Test
    void suggestCompletionRangeOrGroup() {
        List<String> actual = unit.suggestCompletion("1");
        List<String> expected = Arrays.asList("1-", "1,");
        assertElementsMatch(expected, actual);
    }

    @Test
    void suggestCompletionOfRange() {
        assertElementsMatch(Arrays.asList("2-3", "2-4"), unit.suggestCompletion("2-"));
        assertElementsMatch(Arrays.asList("1,2-3", "1,2-4"), unit.suggestCompletion("1,2-"));
        assertElementsMatch(Collections.singletonList("4,2-3"), unit.suggestCompletion("4,2-"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("4,3,2-"));
    }

    @Test
    void suggestCompletionOfGroup() {
        assertElementsMatch(Arrays.asList("3-4,1", "3-4,2"), unit.suggestCompletion("3-4,"));
        assertElementsMatch(Arrays.asList("4,1,2", "4,1,3"), unit.suggestCompletion("4,1,"));
    }

    @Test
    void suggestCompletionNoMore() {
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("1-4"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("4,3,1-2"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("1,4,2,3,2"));
    }

    @Test
    void suggestCompletionEmptyOnError() {
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("1,,"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("5"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("1-2-"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("-1"));
        assertElementsMatch(Collections.emptyList(), unit.suggestCompletion("-1,"));
    }

    @Test
    void toSelectedLines() {
        assertArrayEquals(new int[]{1, 2, 3}, unit.toSelectedLines("2-4"));
        assertArrayEquals(LineSelectorParser.ALL_LINES_SELECTED, unit.toSelectedLines("1,4,4,4,2,3"));
        assertArrayEquals(new int[]{0}, unit.toSelectedLines("1"));
    }

    @Test
    void toSelectedLinesRangeOrderException() {
        assertThrows(RangeOrderLineSelectionException.class, () -> unit.toSelectedLines("3-2"));
    }

    @Test
    void toSelectedLinesRangeParseLineSelectionException() {
        assertThrows(RangeParseLineSelectionException.class, () -> unit.toSelectedLines("2-3-"));
    }

    @Test
    void toSelectedLinesNumberParseLineSelectionException() {
        assertThrows(NumberParseLineSelectionException.class, () -> unit.toSelectedLines("1-a"));
    }

    @Test
    void toSelectedLinesOutOfBoundsLineSelectionException() {
        assertThrows(OutOfBoundsLineSelectionException.class, () -> unit.toSelectedLines("0-3"));

        Configuration config = mock(Configuration.class);
        when(config.getLineStartsAt()).thenReturn(0);
        when(config.getMinLine()).thenCallRealMethod();
        when(config.getMaxLine()).thenCallRealMethod();
        unit = new LineSelectorParser(config);
        assertThrows(OutOfBoundsLineSelectionException.class, () -> unit.toSelectedLines("1-4"));
    }
}