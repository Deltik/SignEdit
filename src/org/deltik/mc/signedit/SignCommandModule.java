package org.deltik.mc.signedit;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import org.deltik.mc.signedit.subcommands.*;

@Module(subcomponents = {SubcommandComponent.class})
public interface SignCommandModule {
    @Binds
    @IntoMap
    @StringKey("set")
    SignSubcommand BindSetSignSubcommand(SetSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("clear")
    SignSubcommand BindClearSignSubcommand(ClearSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("ui")
    SignSubcommand BindUiSignSubcommand(UiSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("cancel")
    SignSubcommand BindCancelSignSubcommand(CancelSignSubcommand command);

//    @Binds
//    @IntoMap
//    @StringKey("status")
//    SignSubcommand BindStatusSignSubcommand(StatusSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("copy")
//    SignSubcommand BindCopySignSubcommand(CopySignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("cut")
//    SignSubcommand BindCutSignSubcommand(CutSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("paste")
//    SignSubcommand BindPasteSignSubcommand(PasteSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("undo")
//    SignSubcommand BindUndoSignSubcommand(UndoSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("redo")
//    SignSubcommand BindRedoSignSubcommand(RedoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("version")
    SignSubcommand BindVersionSignSubcommand(VersionSignSubcommand command);
}
