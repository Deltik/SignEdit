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

package net.deltik.mc.signedit.interactions;

import net.deltik.mc.signedit.subcommands.SubcommandContext;

/**
 * Factory for creating SignEditInteraction instances.
 * Replaces the Dagger-based Map&lt;String, Provider&lt;SignEditInteraction&gt;&gt;.
 */
public class InteractionFactory {
    public static final String INTERACTION_SET = "Set";
    public static final String INTERACTION_COPY = "Copy";
    public static final String INTERACTION_CUT = "Cut";
    public static final String INTERACTION_PASTE = "Paste";
    public static final String INTERACTION_WAX = "Wax";
    public static final String UI_NATIVE = "NativeUi";
    public static final String UI_EDITABLE_BOOK = "EditableBookUi";

    public SignEditInteraction create(String interactionName, SubcommandContext context) {
        switch (interactionName) {
            case INTERACTION_SET:
                return new SetSignEditInteraction(
                        context.signText(),
                        context.services().chatCommsFactory(),
                        context.services().historyManager()
                );
            case INTERACTION_COPY:
                return new CopySignEditInteraction(
                        context.argParser(),
                        context.signText(),
                        context.services().clipboardManager(),
                        context.services().chatCommsFactory()
                );
            case INTERACTION_CUT:
                return new CutSignEditInteraction(
                        context.argParser(),
                        context.signText(),
                        context.services().signEditValidator(),
                        context.services().clipboardManager(),
                        context.services().historyManager(),
                        context.services().chatCommsFactory()
                );
            case INTERACTION_PASTE:
                return new PasteSignEditInteraction(
                        context.services().clipboardManager(),
                        context,
                        context.services().historyManager(),
                        context.services().chatCommsFactory()
                );
            case INTERACTION_WAX:
                return new WaxSignEditInteraction(
                        context.signText(),
                        context.services().chatCommsFactory()
                );
            case UI_NATIVE:
                return new UiSignEditInteraction(
                        context.services().interactionManager(),
                        context.services().chatCommsFactory(),
                        context.signText(),
                        context.services().historyManager()
                );
            case UI_EDITABLE_BOOK:
                return new BookUiSignEditInteraction(
                        context.services().plugin(),
                        context.services().interactionManager(),
                        context.services().chatCommsFactory(),
                        context.signText(),
                        context.services().historyManager()
                );
            default:
                throw new IllegalArgumentException("Unknown interaction: " + interactionName);
        }
    }
}
