package org.deltik.mc.signedit;

import dagger.Component;

import javax.inject.Singleton;

@Component(modules = {SignCommandModule.class})
@Singleton
public interface SignEditPluginComponent {
    void injectSignEditPlugin(SignEditPlugin plugin);
}
