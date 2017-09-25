package org.deltik.mc.SignEdit.Commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.SignEdit.EventHandler.Interact;
import org.deltik.mc.SignEdit.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;


public class SignCommand implements CommandExecutor {
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
            int line = Integer.valueOf(args[1]);

            HashMap<Integer, String> cur = new HashMap<>();

            if (line > 4 || line < 1) {
                p.sendMessage(Main.prefix + "§cLine numbers: 1, 2, 3, 4");
                return true;
            }

            String txt = getTextFromArgs(args);

            if (Main.instance.click()) {
                cur.put(line, txt);
                Interact.sign.put(p, cur);
                p.sendMessage(Main.prefix + "§cNow right-click a block to set the line");
                return true;
            }

            Block b = p.getTargetBlock((Set<Material>) null, 10);

            if (b.getState() instanceof Sign) {
                Sign s = (Sign) b.getState();

                String before = s.getLine(line-1);
                s.setLine(line-1, txt);
                s.update();
                if (txt.isEmpty())
                    p.sendMessage(Main.prefix + "§cLine §e" + line + "§c blanked");
                else if (txt.equals(before))
                    p.sendMessage(Main.prefix + "§cLine §e" + line + "§c unchanged");
                else {
                    p.sendMessage(Main.prefix + "§cLine §e" + line + "§c changed");
                    p.sendMessage(Main.prefix + "§c§lBefore: §r" + before);
                    p.sendMessage(Main.prefix + "§c §l After: §r" + txt);
                }
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
