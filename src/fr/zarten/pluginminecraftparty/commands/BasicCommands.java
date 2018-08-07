package fr.zarten.pluginminecraftparty.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.zarten.pluginminecraftparty.MainGame;
import fr.zarten.pluginminecraftparty.Equipe;

public class BasicCommands implements CommandExecutor {
	int GameStatut = 0;
	int WorldSize = 500;
	int Nbt = 0;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
//COMMANDES INGAME		
		if (sender instanceof Player) {
			Player player = (Player)sender;
// PING			
			if(cmd.getName().equalsIgnoreCase("ping")) {
				player.sendMessage("§4Pong !");				
			}
//BROADCAST			
			else if (cmd.getName().equalsIgnoreCase("alert")) {
				if (args.length == 0) {
					player.sendMessage("La commande est /alert <message>");
				}
				if (args.length >= 1) {
					StringBuilder bc = new StringBuilder();
					for(String part : args) {
						bc.append(part + " ");
					}
					Bukkit.broadcastMessage("[Broadcasting]" + bc.toString());					
				}
			}			
		}
// COMMANDES CMD
		if (!(sender instanceof Player)) {
//GAMESTART		
			if (cmd.getName().equalsIgnoreCase("gamestart")) {
				if (GameStatut == 1) {
					System.out.println("La Partie a deja commence");
				} if (Nbt == 0) {
					System.out.println("Aucune equipe inscrite");
				}
				else {
					GameStatut = 1;
					//TIME SET 0
					Bukkit.getWorld("world").setTime(0);
					Bukkit.broadcastMessage("[Broadcasting]§aGAME START !");				
					
					//FINDING Y SPAWN LOCATION
					int maxHeight = Bukkit.getWorld("world").getMaxHeight();
					int height = maxHeight;
					Location spawn = new Location(Bukkit.getWorld("world"), 0, maxHeight, 0);
					Block block;
					while (height > 0) {
						height--;
						spawn.setY(height);
						block = spawn.getBlock();
						if (!block.isEmpty()) {
							Location spawnIni = new Location(Bukkit.getWorld("world"), 0, height, 0);
							this.SpawnIni(spawnIni);
							spawn.setY(height + 1);
							break;
						}
					}
					//SETTING SPAWN
					Bukkit.getWorld("world").setSpawnLocation(spawn);
					//TELEPORTING PLAYERS
					MainGame mg = new MainGame();
					Player[] ps;

					// Collection<? extends Player> c = Bukkit.getOnlinePlayers();
					// Player[] players = c.toArray(new Player[c.size()]);

					for (Equipe t : mg.getGameteams()) {
						ps = t.getmembres();
						for (Player x : ps) {
							x.getInventory().clear();
							
							x.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 2));
							x.updateInventory();
							x.teleport(spawn);
						}
					}
					World world = Bukkit.getWorld("world");
					WorldBorder wb = world.getWorldBorder();
					wb.setCenter(0,0);
					wb.setSize(WorldSize);
				}
			}
//GAMESTOP
			if (cmd.getName().equalsIgnoreCase("gamestop")) {
				System.out.println("FIN DE LA PARTIE (S'il y en a une)");
				Bukkit.broadcastMessage("[Broadcasting]§aFIN DE LA PARTIE !");
				GameStatut = 0;
				File f = new File("teams");
				f.delete();
				this.Nbt = 0;
			}
//CREATE TEAM
			if (cmd.getName().equalsIgnoreCase("createteam")) {
				if (args.length <= 1) {
					System.out.println("Il manque des Arguments, la commande est /createteam [NomDeLEquipe] [Participant1] [Participant2] ...");
				}
				if (args.length >= 2) {
					String stringBuilder = "L'equipe [" + args[0] + "] a ete cree :";
					System.out.println("Creation de l'equipe : " + args[0]);
					this.Nbt++;
					Player[] p = new Player[args.length-1];
					int i;
					for (i = 1; i < args.length ; i++) {
						stringBuilder = stringBuilder + args[i];
						p[i-1] = Bukkit.getServer().getPlayer(args[i]);
					}
					Bukkit.broadcastMessage(stringBuilder);
					Equipe t = new Equipe(p,args[0]);
					t.saveTeam();
				}
			}
		}
		return false;
	}
	
	public void SpawnIni(Location L) {
		L.getBlock().setType(Material.BEDROCK);
		L.add(1,0,0).getBlock().setType(Material.BEDROCK);
		int i;
		for (i = 0; i<25;i++) {
			L.add(0,1,0).getBlock().setType(Material.BEDROCK);
		}
	}

}



























