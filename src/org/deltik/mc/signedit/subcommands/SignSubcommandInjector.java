package org.deltik.mc.signedit.subcommands;

import dagger.BindsInstance;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.ArgParser;

public interface SignSubcommandInjector<T extends SignSubcommand> {
    T command();

    abstract class Builder<T extends SignSubcommand> {
        public abstract SignSubcommandInjector<T> build();

        @BindsInstance
        public abstract Builder player(Player player);

        @BindsInstance
        public abstract Builder argParser(ArgParser args);
    }
}
