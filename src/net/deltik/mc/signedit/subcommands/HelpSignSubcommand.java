/*
 * Copyright (C) 2017-2024 Deltik <https://www.deltik.net/>
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

import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpSignSubcommand extends SignSubcommand {
    public static final int MAX_LINES = 10;
    private static final Pattern WORD_PATTERN = Pattern.compile("([0-9a-zA-Z]+)");

    private final String signCommandUsage;
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignSubcommandComponent.Builder signSubcommandComponentBuilder;
    private final ArgParser args;
    private final Player player;

    private ChatComms comms;

    @Inject
    public HelpSignSubcommand(
            Plugin self,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignSubcommandComponent.Builder signSubcommandComponent,
            ArgParser args,
            Player player
    ) {
        this(self.getDescription(),
                commsBuilder,
                signSubcommandComponent,
                args,
                player);
    }

    public HelpSignSubcommand(
            PluginDescriptionFile about,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignSubcommandComponent.Builder signSubcommandComponent,
            ArgParser args,
            Player player
    ) {
        this((String) about.getCommands().get(SignCommand.COMMAND_NAME).get("usage"),
                commsBuilder,
                signSubcommandComponent,
                args,
                player);
    }

    public HelpSignSubcommand(
            String signCommandUsage,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignSubcommandComponent.Builder signSubcommandComponent,
            ArgParser args,
            Player player
    ) {
        super(player);
        this.signCommandUsage = signCommandUsage;
        this.commsBuilder = commsBuilder;
        this.signSubcommandComponentBuilder = signSubcommandComponent;
        this.args = args;
        this.player = player;
    }

    @Deprecated
    public HelpSignSubcommand(
            Plugin self,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            ArgParser args,
            Player player
    ) {
        this(self, commsBuilder, null, args, player);
    }

    @Deprecated
    public HelpSignSubcommand(
            String signCommandUsage,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            ArgParser args,
            Player player
    ) {
        this(signCommandUsage, commsBuilder, null, args, player);
    }

    @Override
    public SignEditInteraction execute() {
        this.comms = commsBuilder.commandSender(player).build().comms();
        List<String[]> allowedCommands = getAllowedCommands();

        if (allowedCommands.size() == 0) {
            comms.informForbidden(SignCommand.COMMAND_NAME, args.getSubcommand());
            return null;
        }

        int linesRemaining = MAX_LINES;
        String onlineDocsLine = comms.t("online_documentation", comms.t("online_documentation_url"));
        if (!onlineDocsLine.isEmpty()) {
            comms.tell(onlineDocsLine);
            linesRemaining--;
        }

        linesRemaining--;
        int pageCount = (allowedCommands.size() - 1) / linesRemaining + 1;
        int unsafePageNumber = readInputPageNumber();
        int pageNumber = Integer.min(pageCount, Integer.max(1, unsafePageNumber));
        String pageNumbering;
        if (pageCount > 1) {
            pageNumbering = comms.t("usage_page_numbering", pageNumber, pageCount);
        } else {
            pageNumbering = "";
        }
        comms.tell(
                comms.t("usage_page_heading",
                        comms.t("usage_page_info",
                                SignCommand.COMMAND_NAME,
                                args.getSubcommand(),
                                pageNumbering
                        )
                )
        );

        allowedCommands
                .stream()
                .skip((long) (pageNumber - 1) * linesRemaining)
                .limit(linesRemaining)
                .forEach(this::showSubcommandSyntax);

        return null;
    }

    private int readInputPageNumber() {
        List<String> argsRemainder = args.getRemainder();
        if (argsRemainder.size() == 0) return 1;

        String maybePageNumber = argsRemainder.remove(0);
        try {
            return Integer.parseInt(maybePageNumber);
        } catch (NumberFormatException ignored) {
        }
        return 1;
    }

    public void showSubcommandSyntax(String[] commandSegments) {
        List<String> commandSegmentsList = new ArrayList<>(Arrays.asList(commandSegments));
        if (commandSegmentsList.size() == 0) {
            showSubcommandSyntax(SignCommand.COMMAND_NAME);
            return;
        }
        String command = commandSegmentsList.remove(0);
        if (commandSegmentsList.size() == 0) {
            showSubcommandSyntax(command);
            return;
        }
        String subcommand = commandSegmentsList.remove(0);
        if (commandSegmentsList.size() == 0) {
            showSubcommandSyntax(command, subcommand);
            return;
        }
        showSubcommandSyntax(command, subcommand, commandSegmentsList.toArray(new String[0]));
    }

    public void showSubcommandSyntax(String command) {
        showSubcommandSyntax(command, "");
    }

    public void showSubcommandSyntax(String command, String subcommand) {
        showSubcommandSyntax(command, subcommand, "");
    }

    public void showSubcommandSyntax(String command, String subcommand, String... parameters) {
        String parametersJoined = String.join(" ", parameters);
        comms.tell(comms.t("print_subcommand_usage", command, subcommand, parametersJoined));
    }

    protected List<String[]> getAllowedCommands() {
        List<String[]> allowedCommands = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(signCommandUsage));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("/<command> ")) continue;

                String[] segments = line.split(" ");
                segments[0] = SignCommand.COMMAND_NAME;
                if (segments.length < 2) continue;

                Matcher matcher = WORD_PATTERN.matcher(segments[1]);
                while (matcher.find()) {
                    String subcommandName = matcher.group(1);
                    if (signSubcommandComponentBuilder == null) {
                        if (
                                player.hasPermission("signedit.use") ||
                                player.hasPermission("signedit." + SignCommand.COMMAND_NAME + "." + subcommandName)
                        ) {
                            allowedCommands.add(segments);
                        }
                        continue;
                    }
                    Provider<InteractionCommand> signSubcommandProvider = signSubcommandComponentBuilder
                            .player(player)
                            .commandArgs(new String[]{SignCommand.COMMAND_NAME})
                            .build()
                            .subcommandProviders()
                            .get(subcommandName);
                    if (signSubcommandProvider == null) continue;
                    InteractionCommand signSubcommand = signSubcommandProvider.get();
                    if (signSubcommand.isPermitted()) {
                        allowedCommands.add(segments);
                    }
                }
            }
        } catch (IOException e) {
            this.comms.reportException(e);
        }

        return allowedCommands;
    }
}
