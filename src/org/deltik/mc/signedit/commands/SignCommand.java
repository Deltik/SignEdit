/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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
import org.deltik.mc.signedit.listeners.SignEditListener;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Singleton
public class SignCommand implements CommandExecutor {

    private static final int MAX_DISTANCE = 20;
    private Configuration configuration;
    private final SignEditListener listener;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders;

    @Inject
    public SignCommand(
            Configuration configuration,
            SignEditListener listener,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders
    ) {
        this.listener = listener;
        this.commsBuilderProvider = commsBuilderProvider;
        this.configuration = configuration;
        this.commandBuilders = commandBuilders;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        ChatComms comms = commsBuilderProvider.get().player(player).build().comms();

        ArgParser argParser = new ArgParser(args, configuration, commandBuilders);

        if (!permitted(player, argParser)) {
            comms.informForbidden(command.getName(), argParser.getSubcommand());
            return true;
        }

        Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>> subcommandProvider =
                commandBuilders.get(argParser.getSubcommand());

        if (subcommandProvider == null) {
            comms.showHelpFor(command.getName());
            return true;
        }

        LineSelectionException selectedLinesError = argParser.getLinesSelectionError();
        if (selectedLinesError != null) {
            comms.reportException(selectedLinesError);
            return true;
        }

        SignSubcommandInjector.Builder<? extends SignSubcommand> builder = subcommandProvider.get();

        SignSubcommand subcommand = builder
                .player(player)
                .argParser(argParser)
                .comms(comms)
                .build()
                .command();

        try {
            SignEditInteraction interaction = subcommand.execute();
            autointeract(player, interaction, comms);
        } catch (Exception e) {
            comms.reportException(e);
        }

        return true;
    }

    private boolean permitted(Player player, ArgParser args) {
        // Legacy (< 1.4) permissions
        return (player.hasPermission("SignEdit.use") ||
                // /sign <subcommand>
                player.hasPermission("signedit.sign." + args.getSubcommand()));
    }

    private void autointeract(Player player, SignEditInteraction interaction, ChatComms comms) {
        if (interaction == null) return;

        Block targetBlock = getTargetBlockOfPlayer(player);
        BlockState targetBlockState = null;
        if (targetBlock != null) targetBlockState = targetBlock.getState();
        if (shouldDoClickingMode(targetBlock)) {
            listener.setPendingInteraction(player, interaction);
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
