package org.deltik.mc.signedit.interactions;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.deltik.mc.signedit.exceptions.ForbiddenSignEditException;

import static org.bukkit.Bukkit.getServer;

public interface SignEditInteraction {
    void interact(Player player, Sign sign);

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default void cleanup(Event event) {
    }

    default void validatedInteract(Player player, Sign sign) {
        // Simulates blanking the sign to check if editing this sign is allowed
        SignChangeEvent event = new SignChangeEvent(sign.getBlock(), player, new String[]{"", "", "", ""});
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            throw new ForbiddenSignEditException();
        }
        interact(player, sign);
    }
}
