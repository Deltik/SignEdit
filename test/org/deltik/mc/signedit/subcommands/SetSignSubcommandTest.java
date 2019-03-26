//package org.deltik.mc.signedit.subcommands;

//import org.bukkit.entity.Player;
//import org.deltik.mc.signedit.ArgParser;
//import org.deltik.mc.signedit.Configuration;
//import org.deltik.mc.signedit.listeners.SignEditListener;
//import org.junit.Before;
//import org.junit.Test;

//import java.util.Arrays;

//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;

//public class SetSignSubcommandTest {
//    SignSubcommand subcommand;

//    Configuration config;
//    SignEditListener listener;
//    ArgParser argStruct;
//    Player player;

//    @Before
//    public void setUp() throws Exception {
//        config = mock(Configuration.class);
//        listener = mock(SignEditListener.class);
//        argStruct = mock(ArgParser.class);
//        player = mock(Player.class);

//        when(argStruct.getSubcommand()).thenReturn("set");

//        subcommand = new SetSignSubcommand(config, listener, argStruct, player);
//    }

//    @Test
//    public void signLineShouldUpdateWhenLookingAtSignAndAllowedToEditBySight() {
//        String expected = "alpha bravo charlie";
//        setArgStructLineRelative(1);
//        setArgStructRemainder(expected);

//        subcommand.execute();

//        when(player.getTargetBlock(null, 10)).thenReturn(block);
//        doReturn(true).when(spyConfig).allowedToEditSignBySight();

//        signCommand.onCommand(player, command, cString, argsString.split(" "));

//        verify(sign).setLine(0, "alpha bravo charlie");
//    }

//    private void setArgStructLineRelative(int lineRelative) {
//        when(argStruct.getLineRelative()).thenReturn(lineRelative);
//    }

//    private void setArgStructRemainder(String input) {
//        when(argStruct.getRemainder()).thenReturn(Arrays.asList(input.split(" ")));
//    }
//}