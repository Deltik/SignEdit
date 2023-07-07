/*
 * Copyright (C) 2017-2023 Deltik <https://www.deltik.net/>
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
import net.deltik.mc.signedit.exceptions.ForbiddenWaxedSignEditException;
import net.deltik.mc.signedit.integrations.NoopSignEditValidator;
import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.interactions.SignEditInteraction;
import net.deltik.mc.signedit.listeners.CoreSignEditListener;
import net.deltik.mc.signedit.shims.ISignSide;
import net.deltik.mc.signedit.shims.SideShim;
import net.deltik.mc.signedit.shims.SignHelpers;
import net.deltik.mc.signedit.shims.SignShim;
import net.deltik.mc.signedit.subcommands.PerSubcommand;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PerSubcommand
public class SignText {
    private static final String REGEX_1_HEX = "[0-9a-fA-F]";
    private static final String REGEX_AMP_HEX = "&(" + REGEX_1_HEX + ")";
    private static final String REGEX_6_AMP_HEX = new String(new char[6]).replace("\0", REGEX_AMP_HEX);
    private static final String REGEX_1_CODE = "[0-9A-Fa-fK-Ok-oRrXx]";
    private final SignEditValidator validator;
    private Boolean shouldBeEditable = null;
    private String[] changedLines = new String[4];
    private String[] beforeLines = new String[4];
    private String[] stagedLines = new String[4];
    private String[] afterLines = new String[4];
    @Nullable
    private SignShim targetSign;
    @Nullable
    private SideShim targetSignSide;

    public SignText() {
        this(new NoopSignEditValidator());
    }

    @Inject
    public SignText(SignEditValidator validator) {
        this.validator = validator;
    }

    @Nullable
    public Sign getTargetSign() {
        if (targetSign == null) return null;
        return targetSign.getImplementation();
    }

    @Nullable
    public ISignSide getTargetSignSide() {
        if (targetSign == null || targetSignSide == null) return null;

        return targetSign.getSide(targetSignSide);
    }

    public SideShim getSide() {
        return targetSignSide;
    }

    public void setTargetSign(@Nullable Sign targetSign, @Nullable SideShim targetSignSide) {
        if (targetSign == null) {
            setTargetSign((SignShim) null, targetSignSide);
            return;
        }
        setTargetSign(new SignShim(targetSign), targetSignSide);
    }

    public void setTargetSign(@Nullable SignShim targetSign, @Nullable SideShim targetSignSide) {
        this.targetSign = targetSign;
        this.targetSignSide = targetSignSide != null ? targetSignSide : SideShim.FRONT;
    }

    public void applySignAutoWax(Player player, ChatComms comms) {
        boolean needRewax = false;
        reloadTargetSign();
        if (!SignHelpers.isEditable(Objects.requireNonNull(getTargetSign()))) {
            if (player.hasPermission("signedit.sign.unwax")) {
                SignHelpers.setEditable(getTargetSign(), true);
                getTargetSign().update();
                needRewax = true;
                comms.tell(comms.t("bypass_wax_before"));
            } else {
                throw new ForbiddenWaxedSignEditException();
            }
        }
        applySign();
        if (needRewax) {
            if (player.hasPermission("signedit.sign.wax")) {
                reloadTargetSign();
                SignHelpers.setEditable(getTargetSign(), false);
                getTargetSign().update();
                comms.tell(comms.t("bypass_wax_after"));
            } else {
                comms.tell(comms.t("bypass_wax_cannot_rewax"));
            }
        }
    }

    public void applySign() {
        reloadTargetSign();
        assert getTargetSignSide() != null;
        Sign target = getTargetSign();
        assert target != null;
        if (shouldBeEditable != null) {
            SignHelpers.setEditable(target, shouldBeEditable);
        }
        beforeLines = getTargetSignSide().getLines().clone();
        for (int i = 0; i < changedLines.length; i++) {
            String line = getLine(i);
            if (line != null) {
                getTargetSignSide().setLine(i, line);
            }
        }

        stagedLines = getTargetSignSide().getLines().clone();

        validator.validate(this.targetSign, this.targetSignSide);
        target.update();

        afterLines = getTargetSignSide().getLines().clone();
    }

    private void reloadTargetSign() {
        BlockState newBlockState;
        try {
            newBlockState = getTargetSign() != null ? getTargetSign().getBlock().getState() : null;
        } catch (IllegalStateException ignored) {
            newBlockState = null;
        }

        if (newBlockState instanceof Sign && newBlockState.isPlaced()) {
            targetSign = new SignShim((Sign) newBlockState);
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

    public boolean signTextChanged() {
        return !linesMatch(beforeLines, afterLines);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected static boolean linesMatch(String[] beforeLines, String[] afterLines) {
        return Arrays.equals(beforeLines, afterLines);
    }

    @Nullable
    public Boolean shouldBeEditable() {
        return shouldBeEditable;
    }

    public void setShouldBeEditable(@Nullable Boolean shouldBeEditable) {
        this.shouldBeEditable = shouldBeEditable;
    }

    /**
     * Import sign lines from a {@link SignChangeEvent} that we should mutate because we received an update from a
     * {@link SignEditInteraction}. This is where we run extra validations from {@code /sign ui}.
     *
     * @param event An event from an {@link EventHandler} of any {@link EventPriority} except
     *              {@link EventPriority#MONITOR}
     */
    public void importPendingSignChangeEvent(SignChangeEvent event) {
        targetSign = new SignShim(CoreSignEditListener.getPlacedSignFromBlockEvent(event));
        importSignSide(event);
        String[] lines = event.getLines();
        ISignSide signSide = getTargetSignSide();
        assert signSide != null;
        for (int i = 0; i < lines.length; i++) {
            signSide.setLine(i, this.getLine(i));
            this.setLine(i, lines[i]);
            event.setLine(i, this.getLine(i));
        }
        try {
            validator.validate(event);
        } catch (ForbiddenSignEditException ignored) {
            event.setCancelled(true);
        } catch (Throwable exception) {
            event.setCancelled(true);
            throw exception;
        }
    }

    private void importSignSide(SignChangeEvent event) {
        targetSignSide = SideShim.fromSignChangeEvent(event);
    }

    /**
     * Import sign lines from a {@link SignChangeEvent} for reporting/logging purposes only.
     *
     * @param event An event from an {@link EventHandler} of {@link EventPriority#MONITOR}
     */
    public void importAuthoritativeSignChangeEvent(SignChangeEvent event) {
        targetSign = new SignShim(CoreSignEditListener.getPlacedSignFromBlockEvent(event));
        importSignSide(event);
        assert getTargetSignSide() != null;
        beforeLines = getTargetSignSide().getLines().clone();
        stagedLines = changedLines;
        afterLines = event.getLines();
    }

    public void importSign() {
        ISignSide targetSignSide = getTargetSignSide();
        assert targetSignSide != null;
        changedLines = targetSignSide.getLines().clone();
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
