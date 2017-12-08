package org.deltik.mc.signedit.listeners;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.deltik.mc.signedit.committers.SignEditCommit;

import java.util.HashMap;
import java.util.Map;

public class Interact implements Listener {

    private Map<Player, SignEditCommit> pendingSignEditCommits = new HashMap<>();
    private Map<Player, SignEditCommit> inProgressCommits = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();

        if (isCommitPending(player)) {
            SignEditCommit commit = popSignEditCommit(player);
            commit.validatedCommit(player, sign);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isInProgress(player)) {
            popInProgressCommit(player).cleanup();
        }
        popSignEditCommit(player);
    }

    public void pendSignEditCommit(Player player, SignEditCommit commit) {
        pendingSignEditCommits.put(player, commit);
    }

    public void registerInProgressCommit(Player player, SignEditCommit commit) {
        inProgressCommits.put(player, commit);
    }

    public boolean isCommitPending(Player player) {
        return pendingSignEditCommits.containsKey(player);
    }

    public boolean isInProgress(Player player) {
        return inProgressCommits.containsKey(player);
    }

    public SignEditCommit popSignEditCommit(Player player) {
        return pendingSignEditCommits.remove(player);
    }

    public SignEditCommit popInProgressCommit(Player player) {
        return inProgressCommits.remove(player);
    }
}
