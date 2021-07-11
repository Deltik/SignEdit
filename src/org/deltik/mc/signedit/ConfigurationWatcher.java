/*
 * Copyright (C) 2017-2021 Deltik <https://www.deltik.org/>
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
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bukkit.Bukkit.getLogger;

@Singleton
public class ConfigurationWatcher extends Thread {
    private final Configuration config;
    private final File file;
    private final WatchService watcher;
    private final AtomicBoolean halt = new AtomicBoolean(false);
    private final SignEditPlugin plugin;

    @Inject
    public ConfigurationWatcher(Plugin plugin, Configuration config) {
        this.plugin = (SignEditPlugin) plugin;
        this.config = config;
        this.file = this.config.getConfigFile();

        WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();

            Path fileBasedir = this.file.toPath().getParent();
            fileBasedir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            watcher = null;
            end();

            getLogger().warning(
                    "Could not set up SignEdit configuration watcher. " +
                            "Configuration changes will require a CraftBukkit server restart to apply."
            );
            getLogger().warning(ExceptionUtils.getStackTrace(e));
        }
        this.watcher = watcher;
    }

    public boolean isHalted() {
        return halt.get();
    }

    public void end() {
        halt.set(true);
    }

    @Override
    public void run() {
        while (!isHalted()) {
            WatchKey watchKey = null;
            try {
                watchKey = watcher.take();
            } catch (InterruptedException e) {
                return;
            }
            if (watchKey == null) {
                Thread.yield();
                continue;
            }
            try {
                // noinspection BusyWait
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return;
            }
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                Object context = watchEvent.context();
                if (!(context instanceof Path)) continue;

                Path pathContext = (Path) context;
                if (!pathContext.equals(this.file.toPath().getFileName())) continue;

                if (isHalted()) return;
                getLogger().info("SignEdit detected a configuration file change. Reloading configuration...");
                this.config.reloadConfig();
                plugin.reregisterListeners();
                break;
            }
            watchKey.reset();
            Thread.yield();
        }
    }
}