package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatCommsModule;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class SignCommand implements CommandExecutor {

    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private Configuration configuration;
    private Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders;

    @Inject
    public SignCommand(
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            Configuration configuration,
            Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders
    ) {
        this.commsBuilderProvider = commsBuilderProvider;
        this.configuration = configuration;
        this.commandBuilders = commandBuilders;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        ChatComms comms = commsBuilderProvider.get().player(player).build().comms();

        ArgParser argParser = new ArgParser(args, configuration, commandBuilders);

        if (!permitted(player, argParser)) {
            comms.informForbidden(command.getName(), argParser.getSubcommand());
            return true;
        }

        Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>> subcommandProvider =
                commandBuilders.get(argParser.getSubcommand());

        if (subcommandProvider == null) {
            comms.showHelpFor(command.getName());
            return true;
        }

        SignSubcommandInjector.Builder<? extends SignSubcommand> builder = subcommandProvider.get();

        builder.player(player)
                .argParser(argParser)
                .comms(comms)
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
}
