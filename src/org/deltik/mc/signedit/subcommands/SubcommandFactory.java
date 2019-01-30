package org.deltik.mc.signedit.subcommands;

import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgStruct;
import org.deltik.mc.signedit.SubcommandComponent;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class SubcommandFactory {
    private Provider<SubcommandComponent.Builder> subcommandBuilder;

    @Inject
    public SubcommandFactory(Provider<SubcommandComponent.Builder> subcommandBuilder) {
        this.subcommandBuilder = subcommandBuilder;
    }

    public SignSubcommand createSubcommand(Player player, ArgStruct argStruct) throws ClassNotFoundException {
        Map<String, Provider<SignSubcommand>> subcommands = buildSubcommandMap(player, argStruct);

        if (subcommands.containsKey(argStruct.getSubcommand())) {
            return subcommands.get(argStruct.getSubcommand()).get();
        }

        throw new ClassNotFoundException("Subcommand cannot be resolved from user-provided arguments");
    }

    private Map<String, Provider<SignSubcommand>> buildSubcommandMap(Player player, ArgStruct argStruct) {
        return subcommandBuilder
                    .get()
                    .player(player)
                    .argStruct(argStruct)
                    .build()
                    .subcommandMap();
    }
}
