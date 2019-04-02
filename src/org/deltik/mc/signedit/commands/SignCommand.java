package org.deltik.mc.signedit.commands;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;
import org.deltik.mc.signedit.ChatComms;
import org.deltik.mc.signedit.ChatCommsModule;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.interactions.SignEditInteraction;
import org.deltik.mc.signedit.listeners.SignEditListener;
import org.deltik.mc.signedit.subcommands.SignSubcommand;
import org.deltik.mc.signedit.subcommands.SignSubcommandInjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;

import static org.deltik.mc.signedit.SignEditPlugin.CHAT_PREFIX;

@Singleton
public class SignCommand implements CommandExecutor {

    private Configuration configuration;
    private final SignEditListener listener;
    private final Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider;
    private Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders;

    @Inject
    public SignCommand(
            Configuration configuration,
            SignEditListener listener,
            Provider<ChatCommsModule.ChatCommsComponent.Builder> commsBuilderProvider,
            Map<String, Provider<SignSubcommandInjector.Builder<? extends SignSubcommand>>> commandBuilders
    ) {
        this.listener = listener;
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

        SignSubcommand subcommand = builder.player(player)
                .argParser(argParser)
                .comms(comms)
                .build()
                .command();

        try {
            SignEditInteraction interaction = subcommand.execute();
            autointeract(player, interaction);
        } catch (Exception e) {
            comms.reportException(e);
        }

        return true;
    }

    private boolean permitted(Player player, ArgParser args) {
        // Legacy (< 1.4) permissions
        return (player.hasPermission("SignEdit.use") ||
                // /sign <subcommand>
                player.hasPermission("signedit.sign." + args.getSubcommand()));
    }

    private void autointeract(Player player, SignEditInteraction interaction) {
        if (interaction == null) return;

        Block block = getTargetBlockOfPlayer(player);
        if (shouldDoClickingMode(block)) {
            listener.setPendingInteraction(player, interaction);
            player.sendMessage(CHAT_PREFIX + "§6Now right-click a sign to edit it");
        } else if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            interaction.validatedInteract(player, sign);
        } else {
            player.sendMessage(CHAT_PREFIX + "§cYou must be looking at a sign to edit it!");
        }
    }

    private Block getTargetBlockOfPlayer(Player player) {
        return player.getTargetBlock(null, 10);
    }

    private boolean shouldDoClickingMode(Block block) {
        if (!configuration.allowedToEditSignByRightClick())
            return false;
        else if (block == null)
            return true;
        else return !configuration.allowedToEditSignBySight() || !(block.getState() instanceof Sign);
    }
}
