package org.deltik.mc.signedit.listeners;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.Configuration;
import org.deltik.mc.signedit.subcommands.SetSignSubcommand;

import java.util.HashMap;
import java.util.Map;

public class Interact implements Listener {

    private Configuration config;
    public Map<Player, Map<Integer, String>> pendingSignEdits = new HashMap<>();

    public Interact(Configuration config) {
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign s = (Sign) event.getClickedBlock().getState();

        Player player = event.getPlayer();
        if (pendingSignEdits.containsKey(player)) {
            Map<Integer, String> pendingSignEdit = pendingSignEdits.get(player);
            for (Map.Entry<Integer, String> i : pendingSignEdit.entrySet()) {
                String after = i.getValue();
                SetSignSubcommand.playerEditSignLine(player, s, i.getKey(), after, config);
            }
            pendingSignEdits.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (pendingSignEdits.containsKey(player)) {
            pendingSignEdits.remove(player);
        }
    }
}
