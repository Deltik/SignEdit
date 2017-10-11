package org.deltik.mc.signedit.listeners;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.deltik.mc.signedit.commands.SignCommand;
import org.deltik.mc.signedit.Configuration;

import java.util.HashMap;
import java.util.Map;

public class Interact implements Listener {

    private Configuration config;
    public Map<Player, Map<Integer, String>> pendingSignEdits = new HashMap<>();

    public Interact(Configuration config) {
        this.config = config;
    }

    @EventHandler
    public void onInt(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;

        Sign s = (Sign) e.getClickedBlock().getState();

        Player p = e.getPlayer();
        if (pendingSignEdits.containsKey(p)) {
            Map<Integer, String> pendingSignEdit = pendingSignEdits.get(p);
            for (Map.Entry<Integer, String> i : pendingSignEdit.entrySet()) {
                String after = i.getValue();
                SignCommand.playerEditSignLine(p, s, i.getKey(), after, config);
            }
            pendingSignEdits.remove(e.getPlayer());
        }
    }
}
