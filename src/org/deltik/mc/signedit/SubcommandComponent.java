package org.deltik.mc.signedit;

import dagger.BindsInstance;
import dagger.Subcomponent;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.subcommands.SignSubcommand;

import javax.inject.Provider;
import java.util.Map;

@Subcomponent
public interface SubcommandComponent {
    Map<String, Provider<SignSubcommand>> subcommandMap();

    @Subcomponent.Builder
    public interface Builder {
        SubcommandComponent build();

        @BindsInstance
        Builder player(Player player);

        @BindsInstance
        Builder argStruct(ArgStruct argStruct);
    }
}
