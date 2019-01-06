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

    @Binds
    @IntoMap
    @StringKey("version")
    SignSubcommand BindVersionSignSubcommand(VersionSignSubcommand command);
}
