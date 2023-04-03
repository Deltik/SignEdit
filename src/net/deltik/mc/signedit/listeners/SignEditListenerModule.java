/*
 * Copyright (C) 2017-2023 Deltik <https://www.deltik.net/>
 *
 * This file is part of SignEdit for Bukkit.
 *
 * SignEdit for Bukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SignEdit for Bukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SignEdit for Bukkit.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.deltik.mc.signedit.listeners;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import net.deltik.mc.signedit.Configuration;
import net.deltik.mc.signedit.CraftBukkitReflector;
import net.deltik.mc.signedit.interactions.SignEditInteractionModule;
import net.deltik.mc.signedit.subcommands.UiSignSubcommand;

import javax.inject.Provider;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Module
public abstract class SignEditListenerModule {
    private static final String CORE = "Core";

    @Provides
    @ElementsIntoSet
    static Set<SignEditListener> provideSignEditListeners(
            Map<String, Provider<SignEditListener>> listenerProviders,
            Configuration config,
            CraftBukkitReflector reflector
    ) {
        Set<SignEditListener> listeners = new HashSet<>();
        listeners.add(listenerProviders.get(CORE).get());
        String implementationName = UiSignSubcommand.getImplementationName(config, reflector);
        if (listenerProviders.containsKey(implementationName)) {
            listeners.add(listenerProviders.get(implementationName).get());
        }
        return listeners;
    }

    @Binds
    @IntoMap
    @StringKey(CORE)
    abstract SignEditListener bindCore(CoreSignEditListener listener);

    @Binds
    @IntoMap
    @StringKey(SignEditInteractionModule.UI_EDITABLE_BOOK)
    abstract SignEditListener bindBookUi(BookUiSignEditListener listener);
}
