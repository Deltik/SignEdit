package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.CommandInjector;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.subcommands.SignSubcommand;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

@Singleton
public class SignCommand implements CommandExecutor {

    private Configuration configuration;
    private Map<String, Provider<CommandInjector.Builder<? extends SignSubcommand>>> commandBuilders;

    @Inject
    public SignCommand(Configuration configuration, Map<String, Provider<CommandInjector.Builder<? extends SignSubcommand>>> commandBuilders) {
        this.configuration = configuration;
        this.commandBuilders = commandBuilders;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        ArgParser argParser = new ArgParser(args, configuration, commandBuilders);

        if (!permitted(player, argParser)) {
            informForbidden(player, argParser);
            return true;
        }

        Provider<CommandInjector.Builder<? extends SignSubcommand>> provider = commandBuilders.get(argParser.getSubcommand());

        if (provider == null) {
            sendHelpMessage(player);
            return true;
        }

        CommandInjector.Builder<? extends SignSubcommand> builder = provider.get();

        builder.player(player)
                .argStruct(argParser)
                .build()
                .command()
                .execute();

        return true;
    }

    private boolean permitted(Player player, ArgParser args) {
        // Legacy (< 1.4) permissions
        return (player.hasPermission("SignEdit.use") ||
                // /sign <subcommand>
                player.hasPermission("signedit.sign." + args.getSubcommand()));
    }

    public static void sendHelpMessage(Player p) {
        sendHelpMessage(p, "signedit");
    }

    public static void sendHelpMessage(Player p, String cmdString) {
        p.sendMessage(CHAT_PREFIX + "§f§lUsage:");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[set]§r §7<lines> [<text>]");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e[clear]§r §7<lines>");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §eui");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §ecancel");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §e{copy,cut} §7[<lines>]");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §epaste");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §estatus");
        p.sendMessage(CHAT_PREFIX + "§a§6/" + cmdString + "§r §eversion");
        p.sendMessage(CHAT_PREFIX + "§f§lOnline Help:§r https://git.io/SignEdit-README");
    }

    public static void informForbidden(Player p, ArgParser a) {
        p.sendMessage(CHAT_PREFIX + "§cYou are not allowed to use the §e" + a.getSubcommand() + "§c subcommand.");
    }
}
