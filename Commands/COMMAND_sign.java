package Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import Main.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


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
		if(line > 3){
			
			p.sendMessage(main.prefix + "§cLines Aviable: 0, 1, 2, 3");
			return true;
		}
		 for(int i = 2; i < args.length; i++) txt = txt + args[i].replace("&", "§") + " ";
		 
		 
		 Block b = p.getTargetBlock((HashSet<Byte>) null, 10);
		 
		 if(b.getState() instanceof Sign){
		 
		
			 
			
		 
		
		
		 Sign s = (Sign) b.getState();
		 
		 
		 s.setLine(line, txt.replace("&", "§"));
		 s.update();
		 p.sendMessage(main.prefix + "§cLine §e" + line + " §cchanged to: §r" + txt);
		 }else{
			 p.sendMessage(main.prefix + "§cPlease look at a Sign!");
		 }
		}
	
		
		return false;
	}
	
	

}
