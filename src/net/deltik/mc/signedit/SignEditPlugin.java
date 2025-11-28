/*
 * Copyright (C) 2017-2025 Deltik <https://www.deltik.net/>
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

package net.deltik.mc.signedit;

import net.deltik.mc.signedit.commands.SignCommand;
import net.deltik.mc.signedit.commands.SignCommandTabCompleter;
import net.deltik.mc.signedit.integrations.SignEditValidatorModule;
import net.deltik.mc.signedit.interactions.BookUiSignEditInteraction;
import net.deltik.mc.signedit.interactions.InteractionFactory;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import net.deltik.mc.signedit.listeners.BookUiSignEditListener;
import net.deltik.mc.signedit.listeners.CoreSignEditListener;
import net.deltik.mc.signedit.listeners.SignEditListener;
import net.deltik.mc.signedit.subcommands.SubcommandRegistry;
import net.deltik.mc.signedit.subcommands.UiSignSubcommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public class SignEditPlugin extends JavaPlugin {
    // Singletons - initialized once in onEnable()
    private Configuration config;
    private ConfigurationWatcher configWatcher;
    private UserComms userComms;
    private CraftBukkitReflector reflector;
    private SignTextHistoryManager historyManager;
    private SignTextClipboardManager clipboardManager;
    private SignEditInteractionManager interactionManager;
    private ChatCommsFactory chatCommsFactory;
    private LineSelectorParser lineSelectorParser;
    private InteractionFactory interactionFactory;
    private SignEditPluginServices services;
    private SubcommandRegistry registry;

    private SignCommand signCommand;
    private SignCommandTabCompleter signCommandTabCompleter;

    @Override
    public void onEnable() {
        initializeServices();
        registerCommands();
        reregisterListeners();
        deployUserComms();
        configWatcher.start();
    }

    private void initializeServices() {
        // Configuration
        config = new Configuration(this);
        configWatcher = new ConfigurationWatcher(this, config);
        userComms = new UserComms(this);
        reflector = new CraftBukkitReflector();

        // Managers
        historyManager = new SignTextHistoryManager();
        clipboardManager = new SignTextClipboardManager();
        interactionManager = new SignEditInteractionManager();

        // Factories and parsers
        chatCommsFactory = new ChatCommsFactory(config, userComms);
        interactionManager.setChatCommsFactory(chatCommsFactory);
        lineSelectorParser = new LineSelectorParser(config);
        interactionFactory = new InteractionFactory();

        // Services container
        services = new SignEditPluginServices(
                this,
                config,
                userComms,
                historyManager,
                clipboardManager,
                interactionManager,
                reflector,
                chatCommsFactory,
                lineSelectorParser,
                () -> SignEditValidatorModule.provideSignEditValidator(
                        config.getEditValidation(),
                        getServer().getPluginManager()
                ),
                interactionFactory
        );

        // Registry and command
        registry = new SubcommandRegistry(services);
        signCommand = new SignCommand(
                config,
                interactionManager,
                chatCommsFactory,
                registry
        );
        signCommandTabCompleter = new SignCommandTabCompleter(registry, config);
    }

    private void registerCommands() {
        for (String alias : new String[]{"sign", "signedit", "editsign", "se"}) {
            PluginCommand pluginCommand = this.getCommand(alias);
            if (pluginCommand != null) {
                pluginCommand.setExecutor(signCommand);
                pluginCommand.setTabCompleter(signCommandTabCompleter);
            }
        }
    }

    private void deployUserComms() {
        try {
            userComms.deploy();
        } catch (IOException e) {
            getLogger().warning("Cannot enable user-defined locales due to error:");
            getLogger().warning(getStackTrace(e));
        }
    }

    @Override
    public void onDisable() {
        try {
            configWatcher.end();
            config.configHighstate();
        } catch (IOException e) {
            getLogger().severe(getStackTrace(e));
            throw new IllegalStateException("Unrecoverable error while sanity checking plugin configuration");
        }
    }

    public void reregisterListeners() {
        HandlerList.unregisterAll(this);

        Set<SignEditListener> listeners = createListeners();
        for (SignEditListener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private Set<SignEditListener> createListeners() {
        Set<SignEditListener> listeners = new HashSet<>();

        // Core listener is always registered
        listeners.add(new CoreSignEditListener(services));

        // Book UI listener is only registered when using EditableBook UI mode
        Class<?> uiInteractionClass = UiSignSubcommand.getInteractionClass(config, reflector);
        if (BookUiSignEditInteraction.class.equals(uiInteractionClass)) {
            listeners.add(new BookUiSignEditListener(interactionManager));
        }

        return listeners;
    }

    /**
     * Returns the stack trace of the given {@link Throwable} as a {@link String}
     *
     * @param throwable the {@link Throwable} to be examined
     * @return the stack trace as generated by {@link Throwable#printStackTrace(PrintWriter)}
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    // Getters for testing
    public Configuration getConfig_() {
        return config;
    }

    public SignEditPluginServices getServices() {
        return services;
    }

    public SubcommandRegistry getRegistry() {
        return registry;
    }
}
