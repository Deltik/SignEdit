/*
 * Copyright (C) 2017-2020 Deltik <https://www.deltik.org/>
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
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.commands.SignCommandTabCompleter;
import org.deltik.mc.signedit.listeners.SignEditListener;

import javax.inject.Inject;
import java.io.IOException;

public class SignEditPlugin extends JavaPlugin {
    @Inject
    public Configuration config;
    @Inject
    UserComms userComms;

    @Inject
    public SignEditListener listener;

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
        getServer().getPluginManager().registerEvents(listener, this);

        try {
            userComms.deploy();
        } catch (IOException e) {
            getLogger().warning("Cannot enable user-defined locales due to error:");
            getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onDisable() {
        try {
            config.reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
