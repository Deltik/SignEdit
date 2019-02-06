package org.deltik.mc.signedit;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.multibindings.StringKey;
import org.deltik.mc.signedit.subcommands.*;

import javax.inject.Named;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Module(subcomponents = {SubcommandComponent.class})
abstract class SignCommandModule {
    @Provides
    @ElementsIntoSet
    static Set<Class<? extends SignSubcommand>> provideSignSubcommandClasses() {
        HashSet<Class<? extends SignSubcommand>> signSubcommandClasses = new HashSet<>();

        signSubcommandClasses.add(SetSignSubcommand.class);
        signSubcommandClasses.add(ClearSignSubcommand.class);
        signSubcommandClasses.add(UiSignSubcommand.class);
        signSubcommandClasses.add(CancelSignSubcommand.class);
        signSubcommandClasses.add(VersionSignSubcommand.class);

        return signSubcommandClasses;
    }

    @Provides
    static Map<String, Class<? extends SignSubcommand>> provideSignSubcommandClassMap(
            Set<Class<? extends SignSubcommand>> signSubcommandClasses
    ) {
        HashMap<String, Class<? extends SignSubcommand>> signSubcommandClassMap = new HashMap<>();
        for (Class<? extends SignSubcommand> signSubcommandClass : signSubcommandClasses) {
            String name = signSubcommandClass.getSimpleName();
            name = name.split("SignSubcommand")[0].toLowerCase();
            signSubcommandClassMap.put(name, signSubcommandClass);
        }

        return signSubcommandClassMap;
    }

    @Provides
    @ElementsIntoSet
    @Named("subcommand names")
    static Set<String> provideSignSubcommandNames(Map<String, Class<? extends SignSubcommand>> signSubcommandClassMap) {
        return signSubcommandClassMap.keySet();
    }

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

//    @Binds
//    @IntoMap
//    @StringKey("status")
//    abstract SignSubcommand BindStatusSignSubcommand(StatusSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("copy")
//    abstract SignSubcommand BindCopySignSubcommand(CopySignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("cut")
//    abstract SignSubcommand BindCutSignSubcommand(CutSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("paste")
//    abstract SignSubcommand BindPasteSignSubcommand(PasteSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("undo")
//    abstract SignSubcommand BindUndoSignSubcommand(UndoSignSubcommand command);
//
//    @Binds
//    @IntoMap
//    @StringKey("redo")
//    abstract SignSubcommand BindRedoSignSubcommand(RedoSignSubcommand command);

    @Binds
    @IntoMap
    @StringKey("version")
    abstract SignSubcommand BindVersionSignSubcommand(VersionSignSubcommand command);
}
