package org.deltik.mc.signedit;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

public class SignText {
    private final Player player;
    private final PluginManager pluginManager;
    private String[] changedLines = new String[4];
    private String[] beforeLines = new String[4];
    private String[] afterLines = new String[4];
    private Sign targetSign;

    @Inject
    public SignText(Player player) {
        this(player, player.getServer().getPluginManager());
    }

    public SignText(Player player, PluginManager pluginManager) {
        this.player = player;
        this.pluginManager = pluginManager;
    }

    public Sign getTargetSign() {
        return targetSign;
    }

    public void setTargetSign(Sign targetSign) {
        this.targetSign = targetSign;
    }

    public void applySign() {
        SignChangeEvent signChangeEvent = new SignChangeEvent(
                targetSign.getBlock(),
                player,
                targetSign.getLines().clone()
        );
        applySign(signChangeEvent);
    }

    public void applySign(SignChangeEvent signChangeEvent) {
        if (!Objects.equals(signChangeEvent.getBlock(), targetSign.getBlock())) {
            throw new RuntimeException("Refusing to apply a sign change to a different SignChangeEvent");
        }
        beforeLines = targetSign.getLines().clone();
        for (int i = 0; i < changedLines.length; i++) {
            String line = getLine(i);
            if (line != null) {
                signChangeEvent.setLine(i, line);
                targetSign.setLine(i, line);
            }
        }
        callSignChangeEvent(signChangeEvent);
        targetSign.update();
        afterLines = targetSign.getLines().clone();
    }

    private void callSignChangeEvent(SignChangeEvent signChangeEvent) {
        pluginManager.callEvent(signChangeEvent);
        if (signChangeEvent.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
    }

    public void revertSign() {
        SignChangeEvent signChangeEvent = new SignChangeEvent(targetSign.getBlock(), player, beforeLines);
        for (int i = 0; i < beforeLines.length; i++) {
            if (changedLines[i] != null) {
                targetSign.setLine(i, beforeLines[i]);
            }
        }
        callSignChangeEvent(signChangeEvent);
        targetSign.update();
    }

    public boolean signChanged() {
        return !Arrays.equals(beforeLines, afterLines);
    }

    public void importSign() {
        changedLines = targetSign.getLines().clone();
    }

    public void setLineLiteral(int lineNumber, String value) {
        changedLines[lineNumber] = value;
    }

    public void setLine(int lineNumber, String value) {
        if (value != null) {
            value = value
                    .replaceAll("(?<!\\\\)&([0-9A-Fa-fK-Ok-oRr])", "§$1")
                    .replaceAll("\\\\&(?=[0-9A-Fa-fK-Ok-oRr])", "&");
        }
        setLineLiteral(lineNumber, value);
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

    public String[] getAfterLines() {
        return afterLines;
    }

    public String getLine(int lineNumber) {
        return getLines()[lineNumber];
    }

    public String getBeforeLine(int lineNumber) {
        return getBeforeLines()[lineNumber];
    }

    public String getAfterLine(int lineNumber) {
        return getAfterLines()[lineNumber];
    }

    public String getLineParsed(int lineNumber) {
        String line = getLines()[lineNumber];
        if (line == null) return null;
        line = line
                .replaceAll("&(?=[0-9A-Fa-fK-Ok-oRr])", "\\\\&")
                .replaceAll("§([0-9A-Fa-fK-Ok-oRr])", "&$1");
        return line;
    }
}
