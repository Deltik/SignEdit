package Main;

import org.bukkit.plugin.java.JavaPlugin;

import Commands.COMMAND_sign;

public class main extends JavaPlugin{
	public static String prefix = "§7[§cSignEdit§7] ";
	public void onEnable(){
		System.out.println("Enabling SignEdit");
		this.getCommand("sign").setExecutor(new COMMAND_sign());
	}
	
	public void onDisable(){
		System.out.println("Disabling SignEdit");
	}

}
