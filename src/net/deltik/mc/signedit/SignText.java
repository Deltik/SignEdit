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

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.exceptions.BlockStateNotPlacedException;
import net.deltik.mc.signedit.exceptions.ForbiddenSignEditException;
import net.deltik.mc.signedit.integrations.NoopSignEditValidator;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.subcommands.PerSubcommand;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PerSubcommand
public class SignText {
    private static final String REGEX_1_HEX = "[0-9a-fA-F]";
    private static final String REGEX_AMP_HEX = "&(" + REGEX_1_HEX + ")";
    private static final String REGEX_6_AMP_HEX = new String(new char[6]).replace("\0", REGEX_AMP_HEX);
    private static final String REGEX_1_CODE = "[0-9A-Fa-fK-Ok-oRrXx]";
    private final SignEditValidator validator;
    private String[] changedLines = new String[4];
    private String[] beforeLines = new String[4];
    private String[] stagedLines = new String[4];
    private String[] afterLines = new String[4];
    @Nullable
    private Sign targetSign;

    public SignText() {
        this(new NoopSignEditValidator());
    }

    @Inject
    public SignText(SignEditValidator validator) {
        this.validator = validator;
    }

    @Nullable
    public Sign getTargetSign() {
        return targetSign;
    }

    public void setTargetSign(@Nullable Sign targetSign) {
        this.targetSign = targetSign;
    }

    public void applySign() {
        reloadTargetSign();
        assert getTargetSign() != null;
        beforeLines = getTargetSign().getLines().clone();
        for (int i = 0; i < changedLines.length; i++) {
            String line = getLine(i);
            if (line != null) {
                getTargetSign().setLine(i, line);
            }
        }

        stagedLines = getTargetSign().getLines().clone();

        validator.validate(getTargetSign());
        getTargetSign().update();

        afterLines = getTargetSign().getLines().clone();
    }

    private void reloadTargetSign() {
        BlockState newBlockState;
        try {
            newBlockState = targetSign != null ? targetSign.getBlock().getState() : null;
        } catch (IllegalStateException ignored) {
            newBlockState = null;
        }

        if (newBlockState instanceof Sign && newBlockState.isPlaced()) {
            targetSign = (Sign) newBlockState;
        } else {
            throw new BlockStateNotPlacedException();
        }
    }

    public void revertSign() {
        String[] changedLinesTmp = changedLines.clone();
        for (int i = 0; i < changedLines.length; i++) {
            if (changedLines[i] != null) {
                this.setLineLiteral(i, beforeLines[i]);
            }
        }
        applySign();
        changedLines = changedLinesTmp;
    }

    public boolean signChanged() {
        return !linesMatch(beforeLines, afterLines);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean linesMatch(String[] beforeLines, String[] afterLines) {
        return Arrays.equals(beforeLines, afterLines);
    }

    /**
     * Import sign lines from a {@link SignChangeEvent} that we should mutate because we received an update from a
     * {@link SignEditInteraction}. This is where we run extra validations from {@code /sign ui}.
     *
     * @param event An event from an {@link EventHandler} of any {@link EventPriority} except
     *              {@link EventPriority#MONITOR}
     */
    public void importPendingSignChangeEvent(SignChangeEvent event) {
        setTargetSign((Sign) event.getBlock().getState());
        assert targetSign != null;
        String[] lines = event.getLines();
        for (int i = 0; i < lines.length; i++) {
            targetSign.setLine(i, this.getLine(i));
            this.setLine(i, lines[i]);
            event.setLine(i, this.getLine(i));
        }
        try {
            validator.validate(event);
        } catch (ForbiddenSignEditException ignored) {
            event.setCancelled(true);
        }
    }

    /**
     * Import sign lines from a {@link SignChangeEvent} for reporting/logging purposes only.
     *
     * @param event An event from an {@link EventHandler} of {@link EventPriority#MONITOR}
     */
    public void importAuthoritativeSignChangeEvent(SignChangeEvent event) {
        targetSign = (Sign) event.getBlock().getState();
        beforeLines = targetSign.getLines().clone();
        stagedLines = changedLines;
        afterLines = event.getLines();
    }

    public void importSign() {
        assert targetSign != null;
        changedLines = targetSign.getLines().clone();
    }

    public void setLineLiteral(int lineNumber, String value) {
        changedLines[lineNumber] = value;
    }

    public void setLine(int lineNumber, String line) {
        if (line == null) {
            setLineLiteral(lineNumber, null);
            return;
        }
        line = line.replaceAll("(?<!\\\\)&[Xx]" + REGEX_6_AMP_HEX, "&#$1$2$3$4$5$6");

        Matcher matcher = Pattern.compile("(?<!\\\\)&#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})").matcher(line);
        StringBuffer lineBuffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(lineBuffer, hexToFormattingCode(hex));
        }
        matcher.appendTail(lineBuffer);
        line = lineBuffer.toString();

        line = line
                .replaceAll("(?<!\\\\)&(" + REGEX_1_CODE + ")", "§$1")
                .replaceAll("\\\\&(?=" + REGEX_1_CODE + "|#" + REGEX_1_HEX + "{6}|#" + REGEX_1_HEX + "{3})", "&");

        setLineLiteral(lineNumber, line);
    }

    private String hexToFormattingCode(String hex) {
        StringBuilder builder = new StringBuilder();
        builder.append("§x");
        for (char hexChar : hex.toUpperCase().toCharArray()) {
            builder.append("§").append(hexChar);
            if (hex.length() == 3) builder.append("§").append(hexChar);
        }
        return builder.toString();
    }

    public void clearLine(int lineNumber) {
        changedLines[lineNumber] = null;
    }

    public boolean lineIsSet(int lineNumber) {
        return getLines()[lineNumber] != null;
    }

    public String[] getLines() {
        return changedLines;
    }

    public String[] getBeforeLines() {
        return beforeLines;
    }

    public String[] getStagedLines() {
        return stagedLines;
    }

    public String[] getAfterLines() {
        return afterLines;
    }

    public String getLine(int lineNumber) {
        return getLines()[lineNumber];
    }

    public String getBeforeLine(int lineNumber) {
        return getBeforeLines()[lineNumber];
    }

    public String getStagedLine(int lineNumber) {
        return getStagedLines()[lineNumber];
    }

    public String getAfterLine(int lineNumber) {
        return getAfterLines()[lineNumber];
    }

    public String getLineParsed(int lineNumber) {
        String line = getLines()[lineNumber];
        if (line == null) return null;

        line = line
                .replaceAll("&(?=" + REGEX_1_CODE + "|#" + REGEX_1_HEX + "{6})", "\\\\&")
                .replaceAll("§(" + REGEX_1_CODE + "|#" + REGEX_1_HEX + "{6})", "&$1");

        Matcher matcher = Pattern.compile("&[Xx]((&" + REGEX_1_HEX + "){6})").matcher(line);
        StringBuffer lineBuffer = new StringBuffer();
        while (matcher.find()) {
            String fullMatch = matcher.group();
            matcher.appendReplacement(lineBuffer, formattingCodeToHex(fullMatch));
        }
        matcher.appendTail(lineBuffer);
        line = lineBuffer.toString();

        return line;
    }

    private String formattingCodeToHex(String formattingCode) {
        return formattingCode
                .replace("&", "")
                .replaceFirst("[Xx]", "&#");
    }
}