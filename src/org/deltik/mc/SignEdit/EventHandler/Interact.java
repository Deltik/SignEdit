package org.deltik.mc.SignEdit.EventHandler;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.deltik.mc.SignEdit.Commands.SignCommand;
import org.deltik.mc.SignEdit.Configuration;

import java.util.HashMap;

public class Interact implements Listener {

    public static Configuration config;
    public static HashMap<Player, HashMap<Integer, String>> pendingSignEdits = new HashMap<>();

    @EventHandler
    public void onInt(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;

        Sign s = (Sign) e.getClickedBlock().getState();

        Player p = e.getPlayer();
        if (pendingSignEdits.containsKey(p)) {
            HashMap<Integer, String> pendingSignEdit = pendingSignEdits.get(p);
            for (int i : pendingSignEdit.keySet()) {
                String after = pendingSignEdit.get(i);
                SignCommand.playerEditSignLine(p, s, i, after, config);
            }
            pendingSignEdits.remove(e.getPlayer());
        }
    }
}

