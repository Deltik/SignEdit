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

package org.deltik.mc.signedit.subcommands;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import org.deltik.mc.signedit.interactions.SignEditInteractionModule;

@Module(subcomponents = {
        SignSubcommandModule.SetSignSubcommandComponent.class,
        SignSubcommandModule.ClearSignSubcommandComponent.class,
        SignSubcommandModule.UiSignSubcommandComponent.class,
        SignSubcommandModule.CancelSignSubcommandComponent.class,
        SignSubcommandModule.StatusSignSubcommandComponent.class,
        SignSubcommandModule.CopySignSubcommandComponent.class,
        SignSubcommandModule.CutSignSubcommandComponent.class,
        SignSubcommandModule.PasteSignSubcommandComponent.class,
        SignSubcommandModule.UndoSignSubcommandComponent.class,
        SignSubcommandModule.RedoSignSubcommandComponent.class,
        SignSubcommandModule.VersionSignSubcommandComponent.class,
})
public abstract class SignSubcommandModule {
    @Binds
    @IntoMap
    @StringKey("set")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindSetSignSubcommand(SetSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface SetSignSubcommandComponent extends SignSubcommandInjector<SetSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<SetSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("clear")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindClearSignSubcommand(ClearSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface ClearSignSubcommandComponent extends SignSubcommandInjector<ClearSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<ClearSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("ui")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindUiSignSubcommand(UiSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface UiSignSubcommandComponent extends SignSubcommandInjector<UiSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<UiSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("cancel")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindCancelSignSubcommand(CancelSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface CancelSignSubcommandComponent extends SignSubcommandInjector<CancelSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<CancelSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("status")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindStatusSignSubcommand(StatusSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface StatusSignSubcommandComponent extends SignSubcommandInjector<StatusSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<StatusSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("copy")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindCopySignSubcommand(CopySignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface CopySignSubcommandComponent extends SignSubcommandInjector<CopySignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<CopySignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("cut")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindCutSignSubcommand(CutSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface CutSignSubcommandComponent extends SignSubcommandInjector<CutSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<CutSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("paste")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindPasteSignSubcommand(PasteSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface PasteSignSubcommandComponent extends SignSubcommandInjector<PasteSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<PasteSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("undo")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindUndoSignSubcommand(UndoSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface UndoSignSubcommandComponent extends SignSubcommandInjector<UndoSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<UndoSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("redo")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindRedoSignSubcommand(RedoSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface RedoSignSubcommandComponent extends SignSubcommandInjector<RedoSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<RedoSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("version")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindVersionSignSubcommand(VersionSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent(modules = {SignEditInteractionModule.class})
    public interface VersionSignSubcommandComponent extends SignSubcommandInjector<VersionSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<VersionSignSubcommand> {
        }
    }
}
