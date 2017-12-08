package org.deltik.mc.signedit.committers;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import static org.bukkit.Bukkit.getServer;
import static org.deltik.mc.signedit.Main.CHAT_PREFIX;

public interface SignEditCommit {
    void commit(Player player, Sign sign);

    default void cleanup() {
    }

    default void validatedCommit(Player player, Sign sign) {
        // Simulates blanking the sign to check if editing this sign is allowed
        SignChangeEvent event = new SignChangeEvent(sign.getBlock(), player, new String[]{"", "", "", ""});
        getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.sendMessage(CHAT_PREFIX + "§cSign edit forbidden by policy or other plugin");
            return;
        }
        commit(player, sign);
    }
}
