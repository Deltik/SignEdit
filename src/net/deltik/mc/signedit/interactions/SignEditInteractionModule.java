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

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public abstract class SignEditInteractionModule {
    public static final String UI_NATIVE = "NativeUi";
    public static final String UI_EDITABLE_BOOK = "EditableBookUi";

    @Binds
    @IntoMap
    @StringKey("Set")
    abstract SignEditInteraction bindSet(SetSignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey(UI_EDITABLE_BOOK)
    abstract SignEditInteraction bindEditableBookUi(BookUiSignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey(UI_NATIVE)
    abstract SignEditInteraction bindNativeUi(UiSignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey("Copy")
    abstract SignEditInteraction bindCopy(CopySignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey("Cut")
    abstract SignEditInteraction bindCut(CutSignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey("Paste")
    abstract SignEditInteraction bindPaste(PasteSignEditInteraction interaction);

    @Binds
    @IntoMap
    @StringKey("Wax")
    abstract SignEditInteraction bindWax(WaxSignEditInteraction interaction);
}
