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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;
import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public class SignCommand implements CommandExecutor {
    Configuration config;
    Interact listener;
    private static final Map<String, Class<? extends SignSubcommand>> subcommands;

    static {
        subcommands = new HashMap<>();
        subcommands.put("set", SetSignSubcommand.class);
        subcommands.put("clear", ClearSignSubcommand.class);
    }

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

        if (subcommands.containsKey(argStruct.subcommand)) {
            SignSubcommand subcommand;
            try {
                subcommand = subcommands.get(argStruct.subcommand).getConstructor(Configuration.class, Interact.class, ArgStruct.class, Player.class).newInstance(config, listener, argStruct, p);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                getLogger().warning("Could not construct SignSubcommand \"" + argStruct.subcommand + "\"");
                return false;
            }
            return subcommand.execute();
        } else {
            return sendHelpMessage(p, arg2);
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
        return true;
    }
}
