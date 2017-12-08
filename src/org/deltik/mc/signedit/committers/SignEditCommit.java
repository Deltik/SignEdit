package org.deltik.mc.signedit.committers;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public interface SignEditCommit {
    void commit(Player player, Sign sign);

    default void cleanup() {}
}
