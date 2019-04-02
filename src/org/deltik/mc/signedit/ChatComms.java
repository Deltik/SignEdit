package org.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.exceptions.*;

import javax.inject.Inject;

import static org.bukkit.Bukkit.getLogger;

public class ChatComms {
    private Player player;
    private final Configuration config;

    @Inject
    public ChatComms(Player player, Configuration config) {
        this.player = player;
        this.config = config;
    }

    public void tellPlayer(String message) {
        player.sendMessage(prefix() + message);
    }

    public String prefix() {
        return primaryDark() + "[" +
                primary() + "SignEdit" +
                primaryDark() + "]" +
                reset() + " ";
    }

    public String reset() {
        return "§r";
    }

    public String primary() {
        return "§6";
    }

    public String primaryLight() {
        return "§e";
    }

    public String primaryDark() {
        return "§7";
    }

    public String secondary() {
        return "§f";
    }

    public String strong() {
        return "§l";
    }

    public String error() {
        return "§c";
    }

    public void informForbidden(String command, String subcommand) {
        tellPlayer(error() + "You are not allowed to use " +
                primaryLight() + "/" + command + " " + subcommand +
                error() + "."
        );
    }

    public void showHelpFor(String cmdString) {
        if (cmdString.equals("sign")) {
            tellPlayer(secondary() + strong() + "Usage:");
            showSubcommandSyntax(cmdString, "[set]", "<lines> [<text>]");
            showSubcommandSyntax(cmdString, "[clear]", "<lines>");
            showSubcommandSyntax(cmdString, "ui");
            showSubcommandSyntax(cmdString, "cancel");
            showSubcommandSyntax(cmdString, "{copy,cut}", "[<lines>]");
            showSubcommandSyntax(cmdString, "paste");
            showSubcommandSyntax(cmdString, "status");
            showSubcommandSyntax(cmdString, "version");
            tellPlayer(secondary() + strong() + "Online Help:" +
                    reset() + " " + "https://git.io/SignEdit-README");
        }
    }

    public void showSubcommandSyntax(String command) {
        showSubcommandSyntax(command, "");
    }

    public void showSubcommandSyntax(String command, String subcommand) {
        showSubcommandSyntax(command, subcommand, "");
    }

    public void showSubcommandSyntax(String command, String subcommand, String parameters) {
        String output = primary() + "/" + command;
        if (!subcommand.isEmpty()) {
            output += reset() + " " +
                    primaryLight() + subcommand;
        }
        if (!parameters.isEmpty()) {
            output += reset() + " " +
                    primaryDark() + parameters;
        }
        tellPlayer(output);
    }

    public void reportException(Exception e) {
        if (e instanceof MissingLineSelectionException) {
            tellPlayer(error() + "A line selection is required but was not provided.");
        } else if (e instanceof NumberParseLineSelectionException) {
            tellPlayer(error() + "Cannot parse \"" + e.getMessage() + "\" as a line number");
        } else if (e instanceof OutOfBoundsLineSelectionException) {
            tellPlayer(error() + "Line numbers must be between " + config.getMinLine() + " and " + config.getMaxLine() +
                    ", but " + e.getMessage() + " was provided.");
        } else if (e instanceof RangeOrderLineSelectionException) {
            String lower = ((RangeOrderLineSelectionException) e).getInvalidLowerBound();
            String upper = ((RangeOrderLineSelectionException) e).getInvalidUpperBound();
            tellPlayer(error() + "Lower bound " + lower + " cannot be higher than upper bound " + upper +
                    " in requested selection: " + e.getMessage());
        } else if (e instanceof RangeParseLineSelectionException) {
            String badRange = ((RangeParseLineSelectionException) e).getBadRange();
            tellPlayer(error() + "Invalid range \"" + badRange + "\" in requested selection: " + e.getMessage());
        } else if (e instanceof SignEditorInvocationException) {
            Exception originalException = ((SignEditorInvocationException) e).getOriginalException();
            tellPlayer(error() + strong() + "Failed to invoke sign editor!");
            tellPlayer(primaryDark() + "Likely cause: " + reset() + "Minecraft server API changed");
            tellPlayer(primaryDark() + "Server admin: " + reset() + "Check for updates to this plugin");
            tellPlayer("");
            tellPlayer(primaryDark() + "Error code: " + reset() + originalException.toString());
            tellPlayer(primary() + "(More details logged in server console)");
            getLogger().severe(ExceptionUtils.getStackTrace(originalException));
        } else {
            tellPlayer(error() + "Uncaught error: " + e.toString());
            tellPlayer(error() + "(More details logged in server console)");
            getLogger().severe(ExceptionUtils.getStackTrace(e));
        }
    }
}
