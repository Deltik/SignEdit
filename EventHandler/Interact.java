package EventHandler;

import java.util.HashMap;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import Main.main;

public class Interact implements Listener{
	
	public static HashMap<Player, HashMap<Integer, String>> sign = new HashMap<>();
	
	
	@EventHandler
	public void onInt(PlayerInteractEvent e){
		
		if(!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		
		if(!(e.getClickedBlock().getState() instanceof Sign)) return;
		Sign s = (Sign) e.getClickedBlock().getState();
		
		
		
		if(sign.containsKey(e.getPlayer())){
			HashMap<Integer, String> cur = sign.get(e.getPlayer());
			for(int i : cur.keySet()){
			s.setLine(i, cur.get(i));
			e.getPlayer().sendMessage(main.prefix + "§cLine §e" + i + " §cchanged to: §r" + cur.get(i));
			s.update();
			cur.clear();
			sign.remove(e.getPlayer());
			break;
			}
		}
	}

}
