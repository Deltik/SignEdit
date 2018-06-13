package Commands;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import EventHandler.Interact;
import Main.main;


public class COMMAND_sign implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender cs, Command arg1, String arg2,
			String[] args) {
		if(!(cs instanceof Player)) return true;
		
		Player p = (Player)cs;
		
		if(!p.hasPermission("signedit.use")){
			return true;
		}
		
		if(args.length == 0 | args.length == 1){                    //0   //1   //2
			p.sendMessage(main.prefix + "§c/sign set <Line> <Text>");
			return true;
		}
		
		if(args.length >= 2){
		String txt = "";
		int line = Integer.valueOf(args[1]);
		
		HashMap<Integer, String> cur = new HashMap<>();
		
		if(line > 3){
			
			p.sendMessage(main.prefix + "§cLines Aviable: 0, 1, 2, 3");
			return true;
		}
		 for(int i = 2; i < args.length; i++) txt = txt + args[i].replace("&", "§") + " ";
		 

			if(main.instance.click() == true){
				 cur.put(line, txt);
				 Interact.sign.put(p, cur);
				 p.sendMessage(main.prefix + "§cNow Right Click on a Block to Set the Line.");
				 return true;
			}
			
			StringBuilder sb = new StringBuilder(txt);
			txt = sb.deleteCharAt(sb.length() - 1).toString().replace("&", "§");
		
		
		 Block b = p.getTargetBlock(null, 10);
		 
		 if(b.getState() instanceof Sign){
		 
		
			 
		 
		
		
		 Sign s = (Sign) b.getState();
		 
		 
		 s.setLine(line, txt);
		 s.update();
		 p.sendMessage(main.prefix + "§cLine §e" + line + " §cchanged to: §r" + txt);
		 }else{
			 p.sendMessage(main.prefix + "§cPlease look at a Sign!");
		 }
		}
	
		
		return false;
	}
	
	

}
