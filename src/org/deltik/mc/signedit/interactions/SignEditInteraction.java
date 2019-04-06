package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface SignEditInteraction {
    void interact(Player player, Sign sign);

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default void cleanup(Event event) {
    }
}
