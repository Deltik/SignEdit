package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.SubcommandComponent;
import org.deltik.mc.signedit.subcommands.*;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

@Singleton
public class SignCommand implements CommandExecutor {
    private Provider<SubcommandComponent.Builder> subcommandBuilder;

    @Inject
    public SignCommand(Provider<SubcommandComponent.Builder> subcommandBuilder) {
        this.subcommandBuilder = subcommandBuilder;
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

        Map<String, Provider<SignSubcommand>> subcommands = subcommandBuilder
                .get()
                .player(player)
                .argStruct(argStruct)
                .build()
                .subcommandMap();

        if (subcommands.containsKey(argStruct.subcommand)) {
            SignSubcommand subcommand = subcommands.get(argStruct.subcommand).get();
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
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §eversion");
        return true;
    }

    public static void informForbidden(Player p, ArgStruct a) {
        p.sendMessage(CHAT_PREFIX + "§cYou are not allowed to use the §e" + a.subcommand + "§c subcommand.");
    }
}
