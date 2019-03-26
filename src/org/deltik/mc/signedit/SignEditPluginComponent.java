package org.deltik.mc.signedit;

import dagger.Component;

import javax.inject.Singleton;

@Component(modules = {SignSubcommandModule.class})
@Singleton
public interface SignEditPluginComponent {
    void injectSignEditPlugin(SignEditPlugin plugin);

    @Component.Builder
    public interface Builder {
        //        @BindsInstance Builder args(String[] args);
        SignEditPluginComponent build();
    }
}
