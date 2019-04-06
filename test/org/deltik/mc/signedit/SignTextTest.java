package org.deltik.mc.signedit;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SignTextTest {
    private SignText signText;
    private final String[] defaultSignLines = new String[]{"a", "b", "c", "d"};

    @Before
    public void newTestObject() {
        Player player = mock(Player.class);
        PluginManager pluginManager = mock(PluginManager.class);
        signText = new SignText(player, pluginManager);
    }

    @Test
    public void constructionWithJustPlayerGetsPluginManagerFromPlayer() {
        Player player = mock(Player.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(player.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);

        signText = new SignText(player);

        verify(server, times(1)).getPluginManager();
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
    public void getLinesRetrievesAllLines() {
        String[] expected = new String[]{"alpha", "bravo", "charlie", "delta"};
        for (int i = 0; i < expected.length; i++) {
            signText.setLine(i, expected[i]);
        }

        assertArrayEquals(expected, signText.getLines());
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
        Sign sign = createSign();

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
        Sign sign = createSign();

        signText.setLine(1, "just this line");
        signText.setTargetSign(sign);

        signText.applySign();

        verify(sign, times(0)).setLine(eq(0), any());
        verify(sign, times(1)).setLine(eq(1), eq("just this line"));
        verify(sign, times(0)).setLine(eq(2), any());
        verify(sign, times(0)).setLine(eq(3), any());
    }

    @Test(expected = RuntimeException.class)
    public void applySignValidatesEventIsRelated() {
        Sign sign = mock(Sign.class);
        Block eventBlock = mock(Block.class);
        Block signBlock = mock(Block.class);
        when(sign.getBlock()).thenReturn(signBlock);
        SignChangeEvent event = mock(SignChangeEvent.class);
        when(event.getBlock()).thenReturn(eventBlock);

        signText.setTargetSign(sign);
        signText.applySign(event);
    }

    @Test(expected = ForbiddenSignEditException.class)
    public void throwForbiddenSignEditExceptionWhenSignChangeEventIsCancelled() {
        Sign sign = createSign();
        Player player = mock(Player.class);
        PluginManager pluginManager = mock(PluginManager.class);
        signText = new SignText(player, pluginManager);
        SignChangeEvent signChangeEvent = mock(SignChangeEvent.class);
        Block sameBlock = mock(Block.class);
        when(sign.getBlock()).thenReturn(sameBlock);
        when(signChangeEvent.getBlock()).thenReturn(sameBlock);
        when(signChangeEvent.isCancelled()).thenReturn(true);

        signText.setTargetSign(sign);
        signText.applySign(signChangeEvent);
    }

    @Test
    public void importSign() {
        Sign sign = createSign();

        signText.setTargetSign(sign);
        signText.importSign();

        assertEquals(signText.getLine(0), "a");
        assertEquals(signText.getLine(1), "b");
        assertEquals(signText.getLine(2), "c");
        assertEquals(signText.getLine(3), "d");
    }

    @Test
    public void importSignClonesLines() {
        Sign sign = mock(Sign.class);
        String[] source = new String[]{"a", "b", "c", "d"};
        when(sign.getLines()).thenReturn(source);

        signText.setTargetSign(sign);
        signText.importSign();

        assertNotSame(source, signText.getLines());
    }

    @Test
    public void signBackupAndRestore() {
        Sign sign = createSign();

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

    @Test
    public void signRevertFlipsBeforeAndAfter() {
        Sign sign = createSign();
        signText.setTargetSign(sign);

        signText.setLine(0, "cotton eyed joe");
        signText.applySign();

        assertEquals(defaultSignLines[0], signText.getBeforeLine(0));
        assertEquals("cotton eyed joe", signText.getAfterLine(0));

        signText.revertSign();

        assertEquals("cotton eyed joe", signText.getBeforeLine(0));
        assertEquals(defaultSignLines[0], signText.getAfterLine(0));
    }

    @Test
    public void signChangedReturnsTrueWhenSignChanged() {
        Sign sign = createSign();

        signText.setTargetSign(sign);

        assertFalse(signText.signChanged());
        signText.setLine(1, "Anything else");

        assertFalse(signText.signChanged());
        signText.applySign();

        assertTrue(signText.signChanged());
    }

    @Test
    public void signChangedReturnsFalseWhenSignNotChanged() {
        Sign sign = createSign();

        signText.setTargetSign(sign);

        assertFalse(signText.signChanged());
        signText.setLine(1, "b");

        assertFalse(signText.signChanged());
        signText.applySign();

        assertFalse(signText.signChanged());
    }

    @Test
    public void getBeforeLinesShowBeforeState() {
        Sign sign = createSign();

        signText.setTargetSign(sign);
        signText.setLineLiteral(1, "CHANGED");
        signText.applySign();

        assertArrayEquals(defaultSignLines, signText.getBeforeLines());

        for (int i = 0; i < 4; i++) {
            assertEquals(defaultSignLines[i], signText.getBeforeLine(i));
        }
    }

    @Test
    public void getBeforeLinesShowAfterState() {
        Sign sign = createSign();
        String[] expectedSignLines = defaultSignLines.clone();
        expectedSignLines[1] = "CHANGED";

        signText.setTargetSign(sign);
        signText.setLineLiteral(1, "CHANGED");
        signText.applySign();

        assertArrayEquals(expectedSignLines, signText.getAfterLines());

        for (int i = 0; i < 4; i++) {
            assertEquals(expectedSignLines[i], signText.getAfterLine(i));
        }
    }

    @Test
    public void signSetLineFormatsOnlyFormattingCodes() {
        String[] doOp = "0123456789ABCDEFabcdefKLMNOklmnoRr".split("");
        String[] noOp = "GHIJPQSTUVWXYZghijpqstuvwxyz~!@#$%^&*()_+`-=[]{}\\|;':\"<>,./? ".split("");

        for (String doOpItem : doOp) {
            String input = "&" + doOpItem;
            String expected = "§" + doOpItem;
            signText.setLine(0, input);
            assertEquals(signText.getLine(0), expected);
        }

        for (String noOpItem : noOp) {
            String input = "&" + noOpItem;
            String expected = "&" + noOpItem;
            signText.setLine(0, input);
            assertEquals(signText.getLine(0), expected);
        }
    }

    @Test
    public void signSetLineEscapesOnlyFormattingCodes() {
        String[] doOp = "0123456789ABCDEFabcdefKLMNOklmnoRr".split("");
        String[] noOp = "GHIJPQSTUVWXYZghijpqstuvwxyz~!@#$%^&*()_+`-=[]{}\\|;':\"<>,./? ".split("");

        for (String doOpItem : doOp) {
            String input = "\\&" + doOpItem;
            String expected = "&" + doOpItem;
            signText.setLine(0, input);
            assertEquals(signText.getLine(0), expected);
        }

        for (String noOpItem : noOp) {
            String input = "\\&" + noOpItem;
            String expected = "\\&" + noOpItem;
            signText.setLine(0, input);
            assertEquals(signText.getLine(0), expected);
        }
    }

    @Test
    public void signGetLineParsedParsesOnlyFormattingCodes() {
        String[] doOp = "0123456789ABCDEFabcdefKLMNOklmnoRr".split("");
        String[] noOp = "GHIJPQSTUVWXYZghijpqstuvwxyz~!@#$%^&*()_+`-=[]{}\\|;':\"<>,./? ".split("");

        for (String doOpItem : doOp) {
            String input = "§" + doOpItem;
            String expected = "&" + doOpItem;
            signText.setLineLiteral(0, input);
            assertEquals(signText.getLineParsed(0), expected);
        }

        for (String noOpItem : noOp) {
            String input = "§" + noOpItem;
            String expected = "§" + noOpItem;
            signText.setLineLiteral(0, input);
            assertEquals(signText.getLineParsed(0), expected);
        }
    }

    @Test
    public void signGetLineParsedEscapesOnlyFormattingCodes() {
        String[] doOp = "0123456789ABCDEFabcdefKLMNOklmnoRr".split("");
        String[] noOp = "GHIJPQSTUVWXYZghijpqstuvwxyz~!@#$%^&*()_+`-=[]{}\\|;':\"<>,./? ".split("");

        for (String doOpItem : doOp) {
            String input = "&" + doOpItem;
            String expected = "\\&" + doOpItem;
            signText.setLineLiteral(0, input);
            assertEquals(signText.getLineParsed(0), expected);
        }

        for (String noOpItem : noOp) {
            String input = "&" + noOpItem;
            String expected = "&" + noOpItem;
            signText.setLineLiteral(0, input);
            assertEquals(signText.getLineParsed(0), expected);
        }
    }

    private Sign createSign() {
        Sign sign = mock(Sign.class);
        Block block = mock(Block.class);
        when(sign.getBlock()).thenReturn(block);
        String[] signLines = defaultSignLines.clone();
        when(sign.getLines()).thenReturn(signLines);
        when(sign.getLine(anyInt())).then((Answer) invocation -> signLines[(int) invocation.getArgument(0)]);
        doAnswer(invocation ->
                signLines[(int) invocation.getArgument(0)] = invocation.getArgument(1)
        ).when(sign).setLine(anyInt(), anyString());
        return sign;
    }
}