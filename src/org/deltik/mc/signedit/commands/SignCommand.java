package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;
import org.deltik.mc.signedit.subcommands.SetSignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommand;

import java.util.Arrays;

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

        ArgStruct argStruct = new ArgStruct(args);

        if (!permitted(p, argStruct)) return true;

        if (Arrays.asList("set", "clear").contains(argStruct.subcommand)) {
            SignSubcommand subcommand = new SetSignSubcommand(config, listener, argStruct, p);
            return subcommand.execute();
        } else {
            return sendHelpMessage(p, arg2);
        }
    }

    private boolean permitted(Player player, ArgStruct args) {
        // Legacy (<= 1.3) permissions
        if (player.hasPermission("SignEdit.use")) return true;

        // /sign <subcommand>
        else if (player.hasPermission("signedit.sign." + args.subcommand)) return true;

        // Not permitted
        return false;
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
}
