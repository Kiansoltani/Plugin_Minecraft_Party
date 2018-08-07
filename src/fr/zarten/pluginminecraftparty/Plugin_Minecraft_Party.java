package fr.zarten.pluginminecraftparty;

import org.bukkit.plugin.java.JavaPlugin;

import fr.zarten.pluginminecraftparty.commands.BasicCommands;
import fr.zarten.pluginminecraftparty.tasks.TimedTasks;

public class Plugin_Minecraft_Party extends JavaPlugin {

	@Override
	public void onEnable() {
		
		//COMMANDS
		BasicCommands game = new BasicCommands();
		
		getCommand("ping").setExecutor(new BasicCommands());
		getCommand("alert").setExecutor(new BasicCommands());
		getCommand("gamestart").setExecutor(game);
		getCommand("gamestop").setExecutor(game);
		getCommand("createteam").setExecutor(game);
		//EVENT LISTENER
		getServer().getPluginManager().registerEvents(new Listeners(),this);	
		
		//TIMED TASKS
		TimedTasks task = new TimedTasks();
		task.runTaskTimer(this,0,20);
	}
	@Override
	public void onDisable() {
		System.out.println("PluginTest1 : OFF");
	}
	
}
