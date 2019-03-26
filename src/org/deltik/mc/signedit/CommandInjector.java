package org.deltik.mc.signedit;

import dagger.BindsInstance;
import org.bukkit.entity.Player;
import org.deltik.mc.signedit.subcommands.SignSubcommand;

public interface CommandInjector<T extends SignSubcommand> {
    T command();

    abstract class Builder<T extends SignSubcommand> {
        public abstract CommandInjector<T> build();

        @BindsInstance
        public abstract Builder player(Player player);

        @BindsInstance
        public abstract Builder argStruct(ArgParser args);
    }
}
