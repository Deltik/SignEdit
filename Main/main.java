package Main;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import Commands.COMMAND_sign;
import EventHandler.Interact;

public class main extends JavaPlugin{
	public static String prefix = "§7[§cSignEdit§7] ";
	public static main instance;
	public void onEnable(){
		main.instance = this;
		this.getCommand("sign").setExecutor(new COMMAND_sign());
		getServer().getPluginManager().registerEvents(new Interact(), this);
		createFile();
	}
	
	public void onDisable(){
		
	}
	
	
	private void createFile(){
		
		File file = new File("plugins//" + this.getName() + "//", "config.yml");
		YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
		
	if(!file.exists()){
		c.set("clicking", false);
		
		try {
			c.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	}
	
	

	
	public boolean click(){
		File file = new File("plugins//" + this.getName() + "//", "config.yml");
		YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
		
		return c.getBoolean("clicking");
	}

}
