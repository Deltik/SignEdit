package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.listeners.Interact;
import org.deltik.mc.signedit.subcommands.*;

import java.util.HashMap;
import java.util.Map;

import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class SignCommand implements CommandExecutor {
    private Configuration config;
    private Interact listener;
    private Map<String, SignSubcommand> subcommands;

    public SignCommand(Configuration config, Interact listener) {
        this.config = config;
        this.listener = listener;
        subcommands = new HashMap<>();
        subcommands.put("set", new SetSignSubcommand());
        subcommands.put("clear", new ClearSignSubcommand());
        subcommands.put("ui", new UiSignSubcommand());
        subcommands.put("cancel", new CancelSignSubcommand());
    }

    @Override
    public boolean onCommand(CommandSender cs, Command arg1, String arg2,
                             String[] args) {
        if (!(cs instanceof Player)) return true;
        Player player = (Player) cs;

        ArgStruct argStruct = new ArgStruct(args);

        if (!permitted(player, argStruct)) {
            informForbidden(player, argStruct);
            return true;
        }

        if (subcommands.containsKey(argStruct.subcommand)) {
            SignSubcommand subcommand = subcommands.get(argStruct.subcommand);
            subcommand.setDependencies(config, listener, argStruct, player);
            return subcommand.execute();
        } else {
            return sendHelpMessage(player, arg2);
        }
    }

    private boolean permitted(Player player, ArgStruct args) {
        // Legacy (<= 1.3) permissions
        return (player.hasPermission("SignEdit.use") ||
                // /sign <subcommand>
                player.hasPermission("signedit.sign." + args.subcommand));
    }

    public static boolean sendHelpMessage(Player p) {
        return sendHelpMessage(p, "signedit");
    }

    public static boolean sendHelpMessage(Player p, String cmdString) {
        p.sendMessage(CHAT_PREFIX + "§f§lUsage:");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[set]§r §7<line> [<text>]");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[clear]§r §7<line>");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §eui");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §ecancel");
        return true;
    }

    public static void informForbidden(Player p, ArgStruct a) {
        p.sendMessage(CHAT_PREFIX + "§cYou are not allowed to use the §e" + a.subcommand + "§c subcommand.");
    }
}
