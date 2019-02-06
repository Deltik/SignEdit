package org.deltik.mc.signedit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SignCommandTabCompleter implements TabCompleter {
    private Set<String> subcommandNames;

    @Inject
    public SignCommandTabCompleter(@Named("subcommand names") Set<String> subcommandNames) {
        this.subcommandNames = subcommandNames;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        Set<String> candidateSubcommands = subcommandNames;
        candidateSubcommands = candidateSubcommands
                .stream()
                .filter(
                        name -> name.startsWith(args[0])
                )
                .collect(Collectors.toSet());
        return new ArrayList<>(candidateSubcommands);
    }
}
