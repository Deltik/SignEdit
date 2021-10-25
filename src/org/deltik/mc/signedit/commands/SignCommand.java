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

package org.deltik.mc.signedit.commands;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.ChatCommsModule;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.exceptions.LineSelectionException;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.interactions.SignEditInteractionManager;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandModule;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Singleton
public class SignCommand implements CommandExecutor {
    public static final String COMMAND_NAME = "sign";
    public static final String SUBCOMMAND_NAME_HELP = "help";
    private static final int MAX_DISTANCE = 20;
    private final Configuration configuration;
    private final SignEditInteractionManager interactionManager;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private final SignSubcommandModule.SignSubcommandComponent.Builder signSubcommandComponentBuilder;

    @Inject
    public SignCommand(
            Configuration configuration,
            SignEditInteractionManager interactionManager,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            SignSubcommandModule.SignSubcommandComponent.Builder signSubcommandComponent
    ) {
        this.interactionManager = interactionManager;
        this.commsBuilderProvider = commsBuilderProvider;
        this.configuration = configuration;
        this.signSubcommandComponentBuilder = signSubcommandComponent;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        ChatComms comms = commsBuilderProvider.get().player(player).build().comms();

        SignSubcommandModule.SignSubcommandComponent signSubcommandComponent = signSubcommandComponentBuilder
                .player(player)
                .commandArgs(args)
                .comms(comms)
                .build();

        Map<String, Provider<SignSubcommand>> signSubcommandMap = signSubcommandComponent.subcommandProviders();
        ArgParser argParser = signSubcommandComponent.argParser();
        String subcommandName = argParser.getSubcommand();

        if (!signSubcommandMap.containsKey(subcommandName)) {
            subcommandName = SUBCOMMAND_NAME_HELP;
        }

        if (!permitted(player, subcommandName)) {
            comms.informForbidden(command.getName(), subcommandName);
            return true;
        }

        Provider<? extends SignSubcommand> signSubcommandProvider = signSubcommandMap.get(subcommandName);

        LineSelectionException selectedLinesError = argParser.getLinesSelectionError();
        if (selectedLinesError != null && !subcommandName.equals(SUBCOMMAND_NAME_HELP)) {
            comms.reportException(selectedLinesError);
            return true;
        }

        SignSubcommand signSubcommand = signSubcommandProvider.get();

        try {
            SignEditInteraction interaction = signSubcommand.execute();
            autointeract(player, interaction, comms);
        } catch (Exception e) {
            comms.reportException(e);
        }

        return true;
    }

    public static boolean permitted(Player player, String subcommand) {
        // Legacy (< 1.4) permissions
        return (player.hasPermission("signedit.use") ||
                // /sign <subcommand>
                player.hasPermission("signedit." + COMMAND_NAME + "." + subcommand));
    }

    private void autointeract(Player player, SignEditInteraction interaction, ChatComms comms) {
        if (interaction == null) return;

        Block targetBlock = getTargetBlockOfPlayer(player);
        BlockState targetBlockState = null;
        if (targetBlock != null) targetBlockState = targetBlock.getState();
        if (shouldDoClickingMode(targetBlock)) {
            interactionManager.setPendingInteraction(player, interaction);
            comms.tellPlayer(comms.t("right_click_sign_to_apply_action"));
        } else if (targetBlockState instanceof Sign) {
            interaction.interact(player, (Sign) targetBlockState);
        } else {
            comms.tellPlayer(comms.t("must_look_at_sign_to_interact"));
        }
    }

    @Nullable
    public static Block getTargetBlockOfPlayer(Player player) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method method = Player.class.getMethod("getTargetBlockExact", int.class);
            return (Block) method.invoke(player, MAX_DISTANCE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return player.getTargetBlock(null, MAX_DISTANCE);
        }
    }

    private boolean shouldDoClickingMode(Block block) {
        if (!configuration.allowedToEditSignByRightClick())
            return false;
        else if (block == null)
            return true;
        else return !configuration.allowedToEditSignBySight() || !(block.getState() instanceof Sign);
    }
}
