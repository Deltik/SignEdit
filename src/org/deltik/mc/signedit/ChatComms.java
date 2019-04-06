package org.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.exceptions.*;

import javax.inject.Inject;
import java.util.Objects;

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

    public String highlightBefore() {
        return "§4";
    }

    public String highlightAfter() {
        return "§2";
    }

    public String strong() {
        return "§l";
    }

    public String italic() {
        return "§o";
    }

    public String strike() {
        return "§m";
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

    public void compareSignText(SignText signText) {
        if (!signText.signChanged()) {
            tellPlayer(primary() + "Sign did not change");
        } else {
            String[] beforeHighlights = new String[4];
            String[] afterHighlights = new String[4];
            for (int i = 0; i < 4; i++) {
                if (!Objects.equals(signText.getBeforeLine(i), signText.getAfterLine(i))) {
                    beforeHighlights[i] = highlightBefore();
                    afterHighlights[i] = highlightAfter();
                }
            }
            tellPlayer(primary() + strong() + "Before:");
            dumpLines(signText.getBeforeLines(), beforeHighlights);
            tellPlayer(primary() + strong() + "After:");
            dumpLines(signText.getAfterLines(), afterHighlights);
        }
    }

    public void dumpLines(String[] lines) {
        dumpLines(lines, new String[4]);
    }

    public void dumpLines(String[] lines, String[] highlights) {
        for (int i = 0; i < 4; i++) {
            int relativeLineNumber = config.getLineStartsAt() + i;
            String line = lines[i];
            String highlight = highlights[i];
            if (highlight == null) {
                highlight = secondary();
            }
            if (line == null) {
                line = "";
                highlight = primaryDark() + strike();
            }
            tellPlayer(" " + highlight + "<" + relativeLineNumber + ">" + reset() + " " + line);
        }
    }

    public void reportException(Exception e) {
        if (e instanceof ForbiddenSignEditException) {
            tellPlayer(error() + "Sign edit forbidden by policy or other plugin");
        } else if (e instanceof MissingLineSelectionException) {
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
        } else if (e instanceof SignTextHistoryStackBoundsException) {
            tellPlayer(error() + e.getMessage());
        } else if (e instanceof BlockStateNotPlacedException) {
            tellPlayer(error() + "Operation failed: Sign no longer exists!");
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
