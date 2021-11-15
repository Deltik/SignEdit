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

package net.deltik.mc.signedit.subcommands;

import dagger.Module;
import dagger.*;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import net.deltik.mc.signedit.ArgParser;
import net.deltik.mc.signedit.ArgParserArgs;
import net.deltik.mc.signedit.ChatComms;
import net.deltik.mc.signedit.interactions.SignEditInteractionModule;
import org.bukkit.entity.Player;

import javax.inject.Provider;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module(subcomponents = {
        SignSubcommandModule.SignSubcommandComponent.class,
})
public abstract class SignSubcommandModule {
    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface SignSubcommandComponent {

        Map<String, Provider<SignSubcommand>> subcommandProviders();

        ArgParser argParser();

        @Subcomponent.Builder
        abstract class Builder {
            public abstract SignSubcommandComponent build();

            @BindsInstance
            public abstract SignSubcommandComponent.Builder player(Player player);

            @BindsInstance
            public abstract SignSubcommandComponent.Builder commandArgs(@ArgParserArgs String[] args);

            @BindsInstance
            public abstract SignSubcommandComponent.Builder comms(ChatComms comms);
        }
    }

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
    abstract SignSubcommand BindHelpSignSubcommand(HelpSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("set")
    abstract SignSubcommand BindSetSignSubcommand(SetSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("clear")
    abstract SignSubcommand BindClearSignSubcommand(ClearSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("ui")
    abstract SignSubcommand BindUiSignSubcommand(UiSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("cancel")
    abstract SignSubcommand BindCancelSignSubcommand(CancelSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("status")
    abstract SignSubcommand BindStatusSignSubcommand(StatusSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("copy")
    abstract SignSubcommand BindCopySignSubcommand(CopySignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("cut")
    abstract SignSubcommand BindCutSignSubcommand(CutSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("paste")
    abstract SignSubcommand BindPasteSignSubcommand(PasteSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("undo")
    abstract SignSubcommand BindUndoSignSubcommand(UndoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("redo")
    abstract SignSubcommand BindRedoSignSubcommand(RedoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("version")
    abstract SignSubcommand BindVersionSignSubcommand(VersionSignSubcommand command);
}
