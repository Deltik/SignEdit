package org.deltik.mc.signedit;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import org.bukkit.entity.Player;

@Module(subcomponents = {
        ChatCommsModule.ChatCommsComponent.class
})
public abstract class ChatCommsModule {
    @Subcomponent
    public interface ChatCommsComponent {
        ChatComms comms();

        @Subcomponent.Builder
        abstract class Builder {
            public abstract ChatCommsComponent build();

            @BindsInstance
            public abstract Builder player(Player player);
        }
    }
}
