package org.deltik.mc.signedit.subcommands;

import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module(subcomponents = {
        SignSubcommandModule.SetSignSubcommandComponent.class,
        SignSubcommandModule.ClearSignSubcommandComponent.class,
        SignSubcommandModule.UiSignSubcommandComponent.class,
        SignSubcommandModule.CancelSignSubcommandComponent.class,
        SignSubcommandModule.StatusSignSubcommandComponent.class,
//        SignSubcommandModule.CopySignSubcommandComponent.class,
//        SignSubcommandModule.CutSignSubcommandComponent.class,
//        SignSubcommandModule.PasteSignSubcommandComponent.class,
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
    @Subcomponent
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
    @Subcomponent
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
    @Subcomponent
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
    @Subcomponent
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
    @Subcomponent
    public interface StatusSignSubcommandComponent extends SignSubcommandInjector<StatusSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<StatusSignSubcommand> {
        }
    }

//    @Binds
//    @IntoMap
//    @StringKey("copy")
//    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindCopySignSubcommand(CopySignSubcommandComponent.Builder command);

//    @PerSubcommand
//    @Subcomponent
//    public interface CopySignSubcommandComponent extends SignSubcommandInjector<CopySignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends SignSubcommandInjector.Builder<CopySignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("cut")
//    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindCutSignSubcommand(CutSignSubcommandComponent.Builder command);

//    @PerSubcommand
//    @Subcomponent
//    public interface CutSignSubcommandComponent extends SignSubcommandInjector<CutSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends SignSubcommandInjector.Builder<CutSignSubcommand> {
//        }
//    }

//    @Binds
//    @IntoMap
//    @StringKey("paste")
//    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindPasteSignSubcommand(PasteSignSubcommandComponent.Builder command);

//    @PerSubcommand
//    @Subcomponent
//    public interface PasteSignSubcommandComponent extends SignSubcommandInjector<PasteSignSubcommand> {
//        @Subcomponent.Builder
//        abstract class Builder extends SignSubcommandInjector.Builder<PasteSignSubcommand> {
//        }
//    }

    @Binds
    @IntoMap
    @StringKey("undo")
    abstract SignSubcommandInjector.Builder<? extends SignSubcommand> BindUndoSignSubcommand(UndoSignSubcommandComponent.Builder command);

    @PerSubcommand
    @Subcomponent
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
    @Subcomponent
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
    @Subcomponent
    public interface VersionSignSubcommandComponent extends SignSubcommandInjector<VersionSignSubcommand> {
        @Subcomponent.Builder
        abstract class Builder extends SignSubcommandInjector.Builder<VersionSignSubcommand> {
        }
    }
}
