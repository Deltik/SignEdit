/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.net/>
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

package org.deltik.mc.signedit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.commands.SignCommandTabCompleter;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Set;

public class SignEditPlugin extends JavaPlugin {
    @Inject
    public Configuration config;
    @Inject
    public ConfigurationWatcher configWatcher;
    @Inject
    UserComms userComms;

    @Inject
    public Provider<Set<SignEditListener>> listenersProvider;

    @Inject
    public SignCommand signCommand;
    @Inject
    public SignCommandTabCompleter signCommandTabCompleter;

    @Override
    public void onEnable() {
        DaggerSignEditPluginComponent.builder().plugin(this).build().injectSignEditPlugin(this);

        for (String alias : new String[]{"sign", "signedit", "editsign", "se"}) {
            PluginCommand pluginCommand = this.getCommand(alias);
            pluginCommand.setExecutor(signCommand);
            pluginCommand.setTabCompleter(signCommandTabCompleter);
        }

        reregisterListeners();

        try {
            userComms.deploy();
        } catch (IOException e) {
            getLogger().warning("Cannot enable user-defined locales due to error:");
            getLogger().warning(ExceptionUtils.getStackTrace(e));
        }

        configWatcher.start();
    }

    @Override
    public void onDisable() {
        try {
            configWatcher.end();
            config.configHighstate();
        } catch (IOException e) {
            getLogger().severe(ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unrecoverable error while sanity checking plugin configuration");
        }
    }

    public void reregisterListeners() {
        HandlerList.unregisterAll(this);

        Set<SignEditListener> listeners = listenersProvider.get();
        for (SignEditListener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
