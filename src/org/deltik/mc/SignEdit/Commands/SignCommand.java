package org.deltik.mc.SignEdit.Commands;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


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

        if (args.length <= 1) {
            p.sendMessage(Main.prefix + "§f§lUsage:");
            p.sendMessage(Main.prefix + "§a§6/" + arg2 + "§r §eset§r §7<line> [<text>]");
            p.sendMessage(Main.prefix + "§a§6/" + arg2 + "§r §eclear§r §7<line>");
            return true;
        } else {
            int lineRelative = Integer.valueOf(args[1]);


            int minLine = config.getMinLine();
            int maxLine = config.getMaxLine();
            if (lineRelative > maxLine || lineRelative < minLine) {
                p.sendMessage(Main.prefix + "§cLine numbers are from §e" + minLine + "§c to §e" + maxLine);
                return true;
            }
            int line = lineRelative - minLine;

            String txt = getTextFromArgs(args);

            if (config.allowedToEditSignByRightClick()) {
                HashMap<Integer, String> pendingSignEdit = new HashMap<>();
                pendingSignEdit.put(line, txt);
                Interact.pendingSignEdits.put(p, pendingSignEdit);
                p.sendMessage(Main.prefix + "§cNow right-click a block to set the line");
                return true;
            }

            Block b = p.getTargetBlock(null, 10);

            if (b.getState() instanceof Sign) {
                Sign s = (Sign) b.getState();
                Main.instance.playerEditSignLine(p, s, line, txt);
            } else {
                p.sendMessage(Main.prefix + "§cYou must be looking at a sign to edit it!");
            }
        }

        return false;
}

    private String getTextFromArgs(String[] args) {
        String txt = "";
        if (args.length <= 2 || args[0].equals("clear")) return txt;

        ArrayList<String> textArray = new ArrayList<String>(Arrays.asList(args).subList(2, args.length));
        txt = String.join(" ", textArray).replace('&', '§');

        return txt;
    }
}
