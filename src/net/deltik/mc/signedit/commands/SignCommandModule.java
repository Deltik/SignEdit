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

package net.deltik.mc.signedit.commands;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import net.deltik.mc.signedit.interactions.InteractionCommand;
import net.deltik.mc.signedit.subcommands.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(subcomponents = {
        SignSubcommandComponent.class,
})
public abstract class SignCommandModule {

    /**
     * FIXME: Find a way not to hard-code this Set<String>.
     */
    @Provides
    @SubcommandName
    public static Set<String> provideSubcommandNames() {
        return Stream.of(
                "help",
                "set",
                "clear",
                "ui",
                "cancel",
                "status",
                "copy",
                "cut",
                "paste",
                "undo",
                "redo",
                "version"
        ).collect(Collectors.toSet());
    }

    @Binds
    @IntoMap
    @StringKey("help")
    abstract InteractionCommand BindHelpSignSubcommand(HelpSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("set")
    abstract InteractionCommand BindSetSignSubcommand(SetSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("clear")
    abstract InteractionCommand BindClearSignSubcommand(ClearSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("ui")
    abstract InteractionCommand BindUiSignSubcommand(UiSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("cancel")
    abstract InteractionCommand BindCancelSignSubcommand(CancelSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("status")
    abstract InteractionCommand BindStatusSignSubcommand(StatusSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("copy")
    abstract InteractionCommand BindCopySignSubcommand(CopySignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("cut")
    abstract InteractionCommand BindCutSignSubcommand(CutSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("paste")
    abstract InteractionCommand BindPasteSignSubcommand(PasteSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("undo")
    abstract InteractionCommand BindUndoSignSubcommand(UndoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("redo")
    abstract InteractionCommand BindRedoSignSubcommand(RedoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("version")
    abstract InteractionCommand BindVersionSignSubcommand(VersionSignSubcommand command);
}
