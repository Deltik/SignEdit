package org.deltik.mc.SignEdit.Commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.SignEdit.Configuration;
import org.deltik.mc.SignEdit.EventHandler.Interact;
import org.deltik.mc.SignEdit.Main;

import java.util.*;

public class SignCommand implements CommandExecutor {
    Configuration config;

    public SignCommand(Configuration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command arg1, String arg2,
                             String[] args) {
        if (!(cs instanceof Player)) return true;

        Player p = (Player) cs;

        if (!p.hasPermission("SignEdit.use")) {
            return true;
        }

        String subcommand;
        int lineRelative;
        List<String> argsArray = new LinkedList<>(Arrays.asList(args));
        try {
            subcommand = argsArray.remove(0).toLowerCase();
            if (StringUtils.isNumeric(subcommand)) {
                lineRelative = Integer.valueOf(subcommand);
                subcommand = "set";
            } else {
                lineRelative = Integer.valueOf(argsArray.remove(0));
            }
        } catch (IndexOutOfBoundsException e) {
            subcommand = "help";
            lineRelative = -1;
        } catch (NumberFormatException e) {
            subcommand = "set";
            lineRelative = -1;
        }

        if (subcommand.equals("set") || subcommand.equals("clear")) {
            int minLine = config.getMinLine();
            int maxLine = config.getMaxLine();
            if (lineRelative > maxLine || lineRelative < minLine) {
                p.sendMessage(Main.prefix + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
                return true;
            }
            int line = lineRelative - minLine;

            String txt;
            if (subcommand.equals("clear")) {
                txt = "";
            } else {
                txt = arrayToSignText(argsArray);
            }

            if (config.allowedToEditSignByRightClick()) {
                HashMap<Integer, String> pendingSignEdit = new HashMap<>();
                pendingSignEdit.put(line, txt);
                Interact.config = config;
                Interact.pendingSignEdits.put(p, pendingSignEdit);
                p.sendMessage(Main.prefix + "§cNow right-click a sign to set the line");
                return true;
            }

            Block b = p.getTargetBlock((Set<Material>) null, 10);

            if (b.getState() instanceof Sign) {
                Sign s = (Sign) b.getState();
                playerEditSignLine(p, s, line, txt, config);
            } else {
                p.sendMessage(Main.prefix + "§cYou must be looking at a sign to edit it!");
            }
        } else {
            return sendHelpMessage(p, arg2);
        }

        return false;
    }

    public static boolean sendHelpMessage(Player p) {
        return sendHelpMessage(p, "signedit");
    }

    public static boolean sendHelpMessage(Player p, String cmdString) {
        p.sendMessage(Main.prefix + "§f§lUsage:");
        p.sendMessage(Main.prefix + "§a§6/" + cmdString + "§r §e[set]§r §7<line> [<text>]");
        p.sendMessage(Main.prefix + "§a§6/" + cmdString + "§r §e[clear]§r §7<line>");
        return true;
    }

    private String arrayToSignText(List<String> textArray) {
        return String.join(" ", textArray).replace('&', '§');
    }

    public static void playerEditSignLine(Player p, Sign s, int line, String text, Configuration config) {
        String before = s.getLine(line);
        s.setLine(line, text);
        s.update();
        int lineRelative = line + config.getMinLine();
        if (text.isEmpty())
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c blanked");
        else if (text.equals(before))
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c unchanged");
        else {
            p.sendMessage(Main.prefix + "§cLine §e" + lineRelative + "§c changed");
            p.sendMessage(Main.prefix + "§c§lBefore: §r" + before);
            p.sendMessage(Main.prefix + "§c §l After: §r" + text);
        }
    }
}
