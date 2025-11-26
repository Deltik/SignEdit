/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.SignText;
import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.exceptions.MissingLineSelectionException;
import net.deltik.mc.signedit.interactions.InteractionFactory;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.shims.IBlockHitResult;
import net.deltik.mc.signedit.shims.SideShim;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SignSubcommandInfo(name = "set", supportsLineSelector = true)
public class SetSignSubcommand extends SignSubcommand {
    public SetSignSubcommand(SubcommandContext context) {
        super(context);
    }

    @Override
    public SignEditInteraction execute() {
        int[] selectedLines = argParser().getLinesSelection();
        if (selectedLines.length <= 0) {
            throw new MissingLineSelectionException();
        }

        String text = String.join(" ", argParser().getRemainder());
        SignText signText = context().signText();

        for (int selectedLine : selectedLines) {
            signText.setLine(selectedLine, text);
        }

        return context().services().interactionFactory()
                .create(InteractionFactory.INTERACTION_SET, context());
    }

    @Override
    public List<String> getTabCompletions(ArgParser argParser) {
        IBlockHitResult targetInfo = SignCommand.getLivingEntityTarget(player());
        Block targetBlock = targetInfo.getHitBlock();
        BlockState targetBlockState = null;
        if (targetBlock != null) targetBlockState = targetBlock.getState();
        if (!(targetBlockState instanceof Sign)) {
            return Collections.emptyList();
        }

        Sign targetSign = (Sign) targetBlockState;
        SignText signText = new SignText();
        SideShim side = SideShim.fromRelativePosition(targetSign, player());
        signText.setTargetSign(targetSign, side);
        signText.importSign();

        List<String> qualifyingLines = new ArrayList<>();
        for (int selectedLine : argParser.getLinesSelection()) {
            qualifyingLines.add(signText.getLineParsed(selectedLine));
        }

        String prefix = String.join(" ", argParser.getRemainder());
        return qualifyingLines.stream()
                .filter(line -> line.startsWith(prefix))
                .collect(Collectors.toList());
    }
}
