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

import net.deltik.mc.signedit.integrations.SignEditValidator;
import net.deltik.mc.signedit.interactions.InteractionFactory;
import net.deltik.mc.signedit.interactions.SignEditInteractionManager;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/**
 * Container for singleton services used throughout the plugin.
 * This replaces Dagger's component for providing shared dependencies.
 */
public class SignEditPluginServices {
    private final Plugin plugin;
    private final Configuration config;
    private final UserComms userComms;
    private final SignTextHistoryManager historyManager;
    private final SignTextClipboardManager clipboardManager;
    private final SignEditInteractionManager interactionManager;
    private final CraftBukkitReflector reflector;
    private final ChatCommsFactory chatCommsFactory;
    private final LineSelectorParser lineSelectorParser;
    private final Supplier<SignEditValidator> signEditValidatorSupplier;
    private final InteractionFactory interactionFactory;

    public SignEditPluginServices(
            Plugin plugin,
            Configuration config,
            UserComms userComms,
            SignTextHistoryManager historyManager,
            SignTextClipboardManager clipboardManager,
            SignEditInteractionManager interactionManager,
            CraftBukkitReflector reflector,
            ChatCommsFactory chatCommsFactory,
            LineSelectorParser lineSelectorParser,
            Supplier<SignEditValidator> signEditValidatorSupplier,
            InteractionFactory interactionFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.userComms = userComms;
        this.historyManager = historyManager;
        this.clipboardManager = clipboardManager;
        this.interactionManager = interactionManager;
        this.reflector = reflector;
        this.chatCommsFactory = chatCommsFactory;
        this.lineSelectorParser = lineSelectorParser;
        this.signEditValidatorSupplier = signEditValidatorSupplier;
        this.interactionFactory = interactionFactory;
    }

    public Plugin plugin() {
        return plugin;
    }

    public Configuration config() {
        return config;
    }

    public UserComms userComms() {
        return userComms;
    }

    public SignTextHistoryManager historyManager() {
        return historyManager;
    }

    public SignTextClipboardManager clipboardManager() {
        return clipboardManager;
    }

    public SignEditInteractionManager interactionManager() {
        return interactionManager;
    }

    public CraftBukkitReflector reflector() {
        return reflector;
    }

    public ChatCommsFactory chatCommsFactory() {
        return chatCommsFactory;
    }

    public LineSelectorParser lineSelectorParser() {
        return lineSelectorParser;
    }

    public SignEditValidator signEditValidator() {
        return signEditValidatorSupplier.get();
    }

    public InteractionFactory interactionFactory() {
        return interactionFactory;
    }
}
