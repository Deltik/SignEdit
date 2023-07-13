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

package net.deltik.mc.signedit.integrations;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import net.deltik.mc.signedit.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.inject.Provider;
import java.util.Map;

@Module
public abstract class SignEditValidatorModule {
    @Provides
    static PluginManager providePluginManager(Plugin plugin) {
        return plugin.getServer().getPluginManager();
    }

    @Provides
    static SignEditValidator provideSignEditValidator(
            Configuration config,
            Map<String, Provider<SignEditValidator>> validatorProviders
    ) {
        return validatorProviders.get(config.getEditValidation().toLowerCase()).get();
    }

    @Binds
    @IntoMap
    @StringKey("standard")
    abstract SignEditValidator bindStandard(StandardSignEditValidator integration);

    @Binds
    @IntoMap
    @StringKey("extra")
    abstract SignEditValidator bindExtra(BreakReplaceSignEditValidator integration);

    @Binds
    @IntoMap
    @StringKey("none")
    abstract SignEditValidator bindNone(NoopSignEditValidator integration);
}
