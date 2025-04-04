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

package net.deltik.mc.signedit.commands;

import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.ChatCommsModule;
import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.exceptions.LineSelectionException;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import net.deltik.mc.signedit.shims.*;
import net.deltik.mc.signedit.subcommands.SignSubcommandComponent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
    private final ChatCommsModule.ChatCommsComponent.Builder commsBuilder;
    private final SignSubcommandComponent.Builder signSubcommandComponentBuilder;

    @Inject
    public SignCommand(
            Configuration configuration,
            SignEditInteractionManager interactionManager,
            ChatCommsModule.ChatCommsComponent.Builder commsBuilder,
            SignSubcommandComponent.Builder signSubcommandComponent
    ) {
        this.interactionManager = interactionManager;
        this.commsBuilder = commsBuilder;
        this.configuration = configuration;
        this.signSubcommandComponentBuilder = signSubcommandComponent;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        ChatComms comms = commsBuilder.commandSender(player).build().comms();

        SignSubcommandComponent signSubcommandComponent = signSubcommandComponentBuilder
                .player(player)
                .commandArgs(args)
                .build();

        Map<String, Provider<InteractionCommand>> signSubcommandMap = signSubcommandComponent.subcommandProviders();
        ArgParser argParser = signSubcommandComponent.argParser();
        String subcommandName = argParser.getSubcommand();

        if (!signSubcommandMap.containsKey(subcommandName)) {
            subcommandName = SUBCOMMAND_NAME_HELP;
        }

        Provider<? extends InteractionCommand> signSubcommandProvider = signSubcommandMap.get(subcommandName);

        LineSelectionException selectedLinesError = argParser.getLinesSelectionError();
        if (selectedLinesError != null && !subcommandName.equals(SUBCOMMAND_NAME_HELP)) {
            comms.reportException(selectedLinesError);
            return true;
        }

        InteractionCommand signSubcommand = signSubcommandProvider.get();

        if (!signSubcommand.isPermitted()) {
            comms.informForbidden(command.getName(), subcommandName);
            return true;
        }

        try {
            SignEditInteraction interaction = signSubcommand.execute();
            autointeract(player, interaction, comms);
        } catch (Throwable e) {
            comms.reportException(e);
        }

        return true;
    }

    private void autointeract(Player player, SignEditInteraction interaction, ChatComms comms) {
        if (interaction == null) return;

        IBlockHitResult targetInfo = getLivingEntityTarget(player);
        Block targetBlock = targetInfo.getHitBlock();
        BlockState targetBlockState = null;
        if (targetBlock != null) targetBlockState = targetBlock.getState();
        if (shouldDoClickingMode(targetBlock)) {
            interactionManager.setPendingInteraction(player, interaction);
            comms.tell(comms.t("right_click_sign_to_apply_action"));
        } else if (targetBlockState instanceof Sign) {
            SideShim side = SideShim.fromRelativePosition((Sign) targetBlockState, player);
            interaction.interact(player, new SignShim((Sign) targetBlockState), side);
        } else {
            comms.tell(comms.t("must_look_at_sign_to_interact"));
        }
    }

    @Nullable
    @Deprecated
    public static Block getTargetBlockOfPlayer(Player player) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method method = Player.class.getMethod("getTargetBlockExact", int.class);
            return (Block) method.invoke(player, MAX_DISTANCE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return player.getTargetBlock(null, MAX_DISTANCE);
        }
    }

    public static IBlockHitResult getLivingEntityTarget(LivingEntity entity) {
        try {
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method method = LivingEntity.class.getMethod("rayTraceBlocks", double.class);
            return new PreciseBlockHitResult(method.invoke(entity, MAX_DISTANCE));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return new CalculatedBlockHitResult(entity, MAX_DISTANCE);
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
