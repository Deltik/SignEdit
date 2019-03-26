package org.deltik.mc.signedit;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.deltik.mc.signedit.subcommands.*;

@Module(subcomponents = {
        SignSubcommandModule.SetSignSubcommandComponent.class,
        SignSubcommandModule.ClearSignSubcommandComponent.class,
        SignSubcommandModule.UiSignSubcommandComponent.class,
        SignSubcommandModule.CancelSignSubcommandComponent.class,
//        SignSubcommandModule.StatusSignSubcommandComponent.class,
//        SignSubcommandModule.CopySignSubcommandComponent.class,
//        SignSubcommandModule.CutSignSubcommandComponent.class,
//        SignSubcommandModule.PasteSignSubcommandComponent.class,
//        SignSubcommandModule.UndoSignSubcommandComponent.class,
//        SignSubcommandModule.RedoSignSubcommandComponent.class,
        SignSubcommandModule.VersionSignSubcommandComponent.class,
})
abstract class SignSubcommandModule {
    @Provides
    static SignEditPlugin getSignEditPlugin() {
        return JavaPlugin.getPlugin(SignEditPlugin.class);
    }

    @Binds
    @IntoMap
    @StringKey("set")
    abstract CommandInjector.Builder<? extends SignSubcommand> BindSetSignSubcommand(SetSignSubcommandComponent.Builder command);

    @PerSubCommand
    @Subcomponent
    interface SetSignSubcommandComponent extends CommandInjector<SetSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends CommandInjector.Builder<SetSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("clear")
    abstract CommandInjector.Builder<? extends SignSubcommand> BindClearSignSubcommand(ClearSignSubcommandComponent.Builder command);

    @PerSubCommand
    @Subcomponent
    interface ClearSignSubcommandComponent extends CommandInjector<ClearSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends CommandInjector.Builder<ClearSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("ui")
    abstract CommandInjector.Builder<? extends SignSubcommand> BindUiSignSubcommand(UiSignSubcommandComponent.Builder command);

    @PerSubCommand
    @Subcomponent
    interface UiSignSubcommandComponent extends CommandInjector<UiSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends CommandInjector.Builder<UiSignSubcommand> {
        }
    }

    @Binds
    @IntoMap
    @StringKey("cancel")
    abstract CommandInjector.Builder<? extends SignSubcommand> BindCancelSignSubcommand(CancelSignSubcommandComponent.Builder command);

    @PerSubCommand
    @Subcomponent
    interface CancelSignSubcommandComponent extends CommandInjector<CancelSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends CommandInjector.Builder<CancelSignSubcommand> {
        }
    }

//    @Binds
//    @IntoMap
//    @StringKey("status")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindStatusSignSubcommand(StatusSignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface StatusSignSubcommandComponent extends CommandInjector<StatusSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<StatusSignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("copy")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindCopySignSubcommand(CopySignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface CopySignSubcommandComponent extends CommandInjector<CopySignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<CopySignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("cut")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindCutSignSubcommand(CutSignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface CutSignSubcommandComponent extends CommandInjector<CutSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<CutSignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("paste")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindPasteSignSubcommand(PasteSignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface PasteSignSubcommandComponent extends CommandInjector<PasteSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<PasteSignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("undo")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindUndoSignSubcommand(UndoSignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface UndoSignSubcommandComponent extends CommandInjector<UndoSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<UndoSignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("redo")
//    abstract CommandInjector.Builder<? extends SignSubcommand> BindRedoSignSubcommand(RedoSignSubcommandComponent.Builder command);

//    @PerSubCommand
//    @Subcomponent
//    interface RedoSignSubcommandComponent extends CommandInjector<RedoSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends CommandInjector.Builder<RedoSignSubcommand> {
//        }
//    }

    @Binds
    @IntoMap
    @StringKey("version")
    abstract CommandInjector.Builder<? extends SignSubcommand> BindVersionSignSubcommand(VersionSignSubcommandComponent.Builder command);

    @PerSubCommand
    @Subcomponent
    interface VersionSignSubcommandComponent extends CommandInjector<VersionSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends CommandInjector.Builder<VersionSignSubcommand> {
        }
    }
}
