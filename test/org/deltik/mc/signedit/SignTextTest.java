package org.deltik.mc.signedit;

import org.bukkit.block.Sign;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SignTextTest {
    private SignText signText;

    @Before
    public void newTestObject() {
        signText = new SignText();
    }

    @Test
    public void allLinesOfNewSignTextAreNull() {
        for (int i = 0; i <= 3; i++) {
            assertNull(signText.getLine(i));
            assertFalse(signText.lineIsSet(i));
        }
    }

    @Test
    public void setLineStoresTextInLine() {
        String expected = "Hello";
        int lineNumber = 2;

        signText.setLine(lineNumber, expected);

        assertEquals(expected, signText.getLine(lineNumber));
    }

    @Test
    public void setLineFormatsInputText() {
        String input = "&cHello";
        String output = "§cHello";
        int lineNumber = 0;

        signText.setLine(lineNumber, input);

        assertEquals(output, signText.getLine(lineNumber));
    }

    @Test
    public void setLineLiteralDoesNotFormatInputText() {
        String expected = "&cHello";
        int lineNumber = 0;

        signText.setLineLiteral(lineNumber, expected);

        assertEquals(expected, signText.getLine(lineNumber));
    }

    @Test
    public void lineIsSetChecksIfLineIsSet() {
        signText.setLine(0, "This line is set.");
        signText.setLine(1, null);
        signText.setLine(2, "");
        signText.setLine(3, "content");
        signText.clearLine(3);

        assertTrue(signText.lineIsSet(0));
        assertFalse(signText.lineIsSet(1));
        assertTrue(signText.lineIsSet(2));
        assertFalse(signText.lineIsSet(3));
    }

    @Test
    public void clearLineClearsLine() {
        assertFalse(signText.lineIsSet(0));
        signText.setLine(0, "something's here");
        assertTrue(signText.lineIsSet(0));

        signText.clearLine(0);

        assertFalse(signText.lineIsSet(0));
    }

    @Test
    public void getLineRetrievesLineContents() {
        String expected = "alpha beta gamma";
        int lineNumber = 3;

        signText.setLine(lineNumber, expected);

        assertEquals(expected, signText.getLine(lineNumber));
    }

    @Test
    public void getLineParsedRemovesFormattingFromLineContents() {
        String input = "§cHello";
        String output = "&cHello";
        int lineNumber = 0;

        signText.setLineLiteral(lineNumber, input);

        assertEquals(output, signText.getLineParsed(lineNumber));
    }

    @Test
    public void getLineParsedReturnsNullIfLineIsNotSet() {
        assertNull(signText.getLineParsed(0));
    }

    @Test
    public void targetSignGetterAndSetter() {
        Sign sign = mock(Sign.class);

        assertNull(signText.getTargetSign());

        signText.setTargetSign(sign);

        assertEquals(sign, signText.getTargetSign());
    }

    @Test
    public void applySign() {
        Sign sign = mock(Sign.class);

        signText.setLine(0, "one");
        signText.setLine(1, "two");
        signText.setLine(2, "three");
        signText.setLine(3, "four");
        signText.setTargetSign(sign);

        signText.applySign();

        verify(sign).setLine(0, "one");
        verify(sign).setLine(1, "two");
        verify(sign).setLine(2, "three");
        verify(sign).setLine(3, "four");
    }

    @Test
    public void applySignSkipsUnsetLines() {
        Sign sign = mock(Sign.class);

        signText.setLine(1, "just this line");
        signText.setTargetSign(sign);

        signText.applySign();

        verify(sign, times(0)).setLine(eq(0), any());
        verify(sign, times(1)).setLine(eq(1), eq("just this line"));
        verify(sign, times(0)).setLine(eq(2), any());
        verify(sign, times(0)).setLine(eq(3), any());
    }

    @Test
    public void importSign() {
        Sign sign = mock(Sign.class);
        when(sign.getLines()).thenReturn(new String[]{"a", "b", "c", "d"});

        signText.setTargetSign(sign);
        signText.importSign();

        assertEquals(signText.getLine(0), "a");
        assertEquals(signText.getLine(1), "b");
        assertEquals(signText.getLine(2), "c");
        assertEquals(signText.getLine(3), "d");
    }

    @Test
    public void signBackupAndRestore() {
        Sign sign = mock(Sign.class);
        String[] signContents = new String[]{"a", "b", "c", "d"};
        when(sign.getLines()).thenReturn(signContents);
        when(sign.getLine(anyInt())).then((Answer) invocation -> signContents[(int) invocation.getArgument(0)]);

        signText.setTargetSign(sign);

        signText.setLine(1, "2");
        signText.setLine(3, "4");
        signText.applySign();

        verify(sign, times(0)).setLine(eq(0), any());
        verify(sign, times(1)).setLine(eq(1), eq("2"));
        verify(sign, times(0)).setLine(eq(2), any());
        verify(sign, times(1)).setLine(eq(3), eq("4"));

        signText.revertSign();

        verify(sign, times(0)).setLine(eq(0), any());
        verify(sign, times(1)).setLine(eq(1), eq("b"));
        verify(sign, times(0)).setLine(eq(2), any());
        verify(sign, times(1)).setLine(eq(3), eq("d"));
    }
}