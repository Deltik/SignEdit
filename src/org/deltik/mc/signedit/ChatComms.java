package org.deltik.mc.signedit;

import org.bukkit.entity.Player;

import javax.inject.Inject;

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
}
