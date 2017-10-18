package org.deltik.mc.signedit.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class SignCommand implements CommandExecutor {
    Configuration config;
    Interact listener;

    public SignCommand(Configuration config, Interact listener) {
        this.config = config;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command arg1, String arg2,
                             String[] args) {
        if (!(cs instanceof Player)) return true;

        Player p = (Player) cs;
        Block b = p.getTargetBlock((Set<Material>) null, 10);

        ArgStruct argStruct = new ArgStruct(args);

        if (!permitted(p, argStruct)) return true;

        if (Arrays.asList("set", "clear").contains(argStruct.subcommand)) {
            int minLine = config.getMinLine();
            int maxLine = config.getMaxLine();
            if (argStruct.lineRelative > maxLine || argStruct.lineRelative < minLine) {
                p.sendMessage(CHAT_PREFIX + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
                return true;
            }
            int line = argStruct.lineRelative - minLine;

            String txt;
            if (argStruct.subcommand.equals("clear")) {
                txt = "";
            } else {
                txt = arrayToSignText(argStruct.remainder);
            }


            if (shouldDoClickingMode(b)) {
                return pendSignEdit(p, line, txt);
            } else if (b.getState() instanceof Sign) {
                Sign s = (Sign) b.getState();
                playerEditSignLine(p, s, line, txt, config);
            } else {
                p.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to edit it!");
            }
        } else {
            return sendHelpMessage(p, arg2);
        }

        return false;
    }

    private boolean permitted(Player player, ArgStruct args) {
        // Legacy (<= 1.3) permissions
        if (player.hasPermission("SignEdit.use")) return true;

        // /sign <subcommand>
        if (player.hasPermission("signedit.sign." + args.subcommand)) return true;

        // Not permitted
        return false;
    }

    private boolean pendSignEdit(Player player, int line, String text) {
        HashMap<Integer, String> pendingSignEdit = new HashMap<>();
        pendingSignEdit.put(line, text);
        listener.pendingSignEdits.put(player, pendingSignEdit);
        player.sendMessage(CHAT_PREFIX + "§cNow right-click a sign to set the line");
        return true;
    }

    public static boolean sendHelpMessage(Player p) {
        return sendHelpMessage(p, "signedit");
    }

    public static boolean sendHelpMessage(Player p, String cmdString) {
        p.sendMessage(CHAT_PREFIX + "§f§lUsage:");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[set]§r §7<line> [<text>]");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[clear]§r §7<line>");
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
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c blanked");
        else if (text.equals(before))
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c unchanged");
        else {
            p.sendMessage(CHAT_PREFIX + "§cLine §e" + lineRelative + "§c changed");
            p.sendMessage(CHAT_PREFIX + "§c§lBefore: §r" + before);
            p.sendMessage(CHAT_PREFIX + "§c §l After: §r" + text);
        }
    }

    private boolean shouldDoClickingMode(Block block) {
        if (!config.allowedToEditSignByRightClick())
            return false;
        else if (block == null)
            return true;
        else if (config.allowedToEditSignBySight() && block.getState() instanceof Sign)
            return false;
        return true;
    }
}
