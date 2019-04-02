package org.deltik.mc.signedit;

import dagger.BindsInstance;
import dagger.Component;
import org.deltik.mc.signedit.subcommands.SignSubcommandModule;

import javax.inject.Singleton;

@Component(modules = {
        SignSubcommandModule.class,
        ChatCommsModule.class
})
@Singleton
public interface SignEditPluginComponent {
    void injectSignEditPlugin(SignEditPlugin plugin);

    @Component.Builder
    interface Builder {
        SignEditPluginComponent build();

        @BindsInstance
        Builder plugin(SignEditPlugin plugin);
    }
}
