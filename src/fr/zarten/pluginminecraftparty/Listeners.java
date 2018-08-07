package fr.zarten.pluginminecraftparty;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Llama;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Listeners implements Listener {
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if(p.isDead()) {
			if (p.getKiller() instanceof Player) {

				if (p.getWorld() == Bukkit.getWorld("world")) {

					// ZOMBIE SPAWN
					// SPAWN LOCATION
					Player killer = p.getKiller();
					Location Lk = killer.getLocation();
					spawnZombieAroundSurface(Lk, 8, 15);

					if (p.getName().equals("Wendy31")) {
						Bukkit.broadcastMessage("[Broadcasting]§a" + p.getKiller().getName() + " a tué " + p.getName()
								+ ". Ce n'était pas compliqué...");
					} else {
						Bukkit.broadcastMessage("[Broadcasting]§a" + p.getKiller().getName() + " a tué " + p.getName());
					}
				}else if (p.getWorld() == Bukkit.getWorld("Mini_Games")) {
					//DETECT ARENE KILL
					if (p.getLocation().distance(new Location(Bukkit.getWorld("Mini_Games"), 4, 6, 67)) < 34) {
						MJArenePlayerKill(p.getName());
					}
				}
            }
			if (event.getEntity().getWorld() == Bukkit.getWorld("Mini_Games") && (event.getEntity() instanceof Player)){
				MainGame mg = new MainGame();
				int NumberOfTeams = mg.getNumberOfTeams();

				// SHEEPS VS PIGS
				int i;
				for (i = 1; i <= NumberOfTeams; i++) {
					Location L1 = MJSheepVsPigMonsterLocations(i);
					Location L2 = MJSheepVsPigMonsterLocations(i);
					L2.setY(5);
					if (event.getEntity().getLocation().distance(L1) < 50
							|| event.getEntity().getLocation().distance(L2) < 60) {

						// GOLD = GOLD/4
						try {
							File f = new File("MJ2_SheepVsPig_Money");
							File f2 = new File("MJ2_SheepVsPig_Money_TEMP");
							FileWriter fw2 = new FileWriter(f2);
							FileReader fr = new FileReader(f);
							BufferedReader br = new BufferedReader(fr);
							String line;
							int money;
							int j;
							for (j = 1; j <= NumberOfTeams; j++) {
								line = br.readLine();
								if (j == i) {
									money = Integer.parseInt(line);
									money = money / 2;
									line = String.valueOf(money);
									Player[] ps = mg.getTeambyTeamNumber(i).getmembres();
									for (Player player : ps) {

										player.sendMessage("[DEATH] " + event.getEntity().getName() + " est mort. Vous avez perdu §e" + money + "g§f.");
									}
								}
								fw2.write(line);
								fw2.write("\r\n");
							}
							br.close();
							fr.close();
							fw2.close();
							f.delete();
							f2.renameTo(f);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
        }	
	}
	
	@EventHandler 
	public void onEntityDeath(EntityDeathEvent event) throws IOException{
		if (event.getEntity().getKiller() instanceof Player
				&& event.getEntity().getWorld() == Bukkit.getWorld("Mini_Games")
				&& !(event.getEntity() instanceof Player)) {
			MainGame mg = new MainGame();
			int NumberOfTeams = mg.getNumberOfTeams();
			int i;
			int KillerTeamNumber = mg.getTeamNumberByTeamPlayer(event.getEntity().getKiller());
			if (KillerTeamNumber > 0) {

				double distance;
				// CHECK IF ENTITY
				for (i = 1; i <= NumberOfTeams; i++) {
					distance = MJSheepVsPigMonsterLocations(i).distance(event.getEntity().getLocation());
					if (distance < 50) {
						MJSheepVsPigOnEntityDeath(mg, event);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSignShops(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if ((event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN) && event.getClickedBlock().getWorld() == Bukkit.getWorld("Mini_Games")) {
				SignShopClick(event);
			}
			Block chest = event.getClickedBlock();
			if (chest.getType() == Material.CHEST) {
				if (isMagicChest(event.getClickedBlock().getLocation())) {
					spawnZombieAroundSurface(event.getClickedBlock().getLocation(), 8, 8);
				}
			}
		}		
	}
	
	@EventHandler
	public void onSnowBall(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Snowball ) {
			Block block;
			Entity entity;
			Location hitLocation = null;
			block = event.getHitBlock();

			if (block != null) {
				hitLocation = block.getLocation();
			} else {
				entity = event.getHitEntity();
				if (entity != null) {
					hitLocation = entity.getLocation();
				}
			}
			if (hitLocation != null) {
				Entity[] entities = getNearbyEntities(hitLocation, 4);

				int i;
				Location knockedLocation;
				double vectorx;
				double vectory;
				double vectorz;
				double x;
				double z;
				double y;
				double coeff = 5;
				double distance;
				Vector vector = new Vector(0, 0, 0);
				for (i = 0; i < entities.length; i++) {
					if (!entities[i].getName().equals("Snowball")) {
						knockedLocation = entities[i].getLocation();
						
						x = (knockedLocation.getX() - hitLocation.getX());
						y = (knockedLocation.getY() - hitLocation.getY());
						z = (knockedLocation.getZ() - hitLocation.getZ());
						distance = Math.sqrt(x*x+y*y+z*z);
						
						if (distance != 0) {
							x = x*coeff/distance;
							y = y*coeff/distance;
							z = z*coeff/distance;							
						} else {
							x = 0;
							y = 0.7;
							z = 0;
						}

						vectorx = bord(x,3);
						vectory = bord(y,0.7);
						vectorz = bord(z,3);
						
						vector.setX(vectorx);
						vector.setY(vectory);
						vector.setZ(vectorz);
						// KNOCKING
						entities[i].setVelocity(vector);
					}
				}
			}
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) throws IOException {
	//	String playerName = event.getPlayer().getName();
		if (event.getPlayer().getWorld() == Bukkit.getWorld("Mini_Games")) {
			MainGame mg = new MainGame();
			int NumberOfTeams = mg.getNumberOfTeams();
			//ARENA
			if (event.getPlayer().getLocation().distance(new Location(Bukkit.getWorld("Mini_Games"), 4, 4, 64)) < 50) {
				event.setRespawnLocation(new Location(Bukkit.getWorld("Mini_Games"), 0, 9, 50));
			} else if (event.getPlayer().getLocation().distance(new Location(Bukkit.getWorld("Mini_Games"), -10, 4, 13)) < 15 || event.getPlayer().getLocation().distance(new Location(Bukkit.getWorld("Mini_Games"), -10, 4, 18)) < 15) {
				//STOMP THEM ALL
				MJStompThemAllPlayerLost(event.getPlayer());
				event.setRespawnLocation(new Location(Bukkit.getWorld("Mini_Games"),-32,5,16));
			} else {
				// SHEEPS VS PIGS
				int i;
				for (i = 1; i <= NumberOfTeams; i++) {
					Location L1 = MJSheepVsPigMonsterLocations(i);
					Location L2 = MJSheepVsPigMonsterLocations(i);
					L2.setY(5);
					if (event.getPlayer().getLocation().distance(L1) < 50
							|| event.getPlayer().getLocation().distance(L2) < 60) {
						System.out.println("DIST1 : " + event.getPlayer().getLocation().distance(L1));
						System.out.println("DIST2 : " + event.getPlayer().getLocation().distance(L2));
						event.getPlayer().getInventory().addItem(new ItemStack(Material.WOOD_SWORD, 1));
						event.getPlayer().getInventory().addItem(new ItemStack(Material.APPLE, 5));
						event.setRespawnLocation(MJSheepVsPigPlayerLocations(i));
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e)
	{
	        if(!e.getRightClicked().isVisible())
	        {
	            e.setCancelled(true);
	        }
	}
	
	public void spawnZombieAroundSurface(Location spawnLocation, int number, int spread) {
		
		int i;
		Random r = new Random();
		int maxy = Bukkit.getWorld("world").getMaxHeight();
		Block block;
		boolean continuer;
    	for (i = 0; i <number; i++) {
    		int x = r.nextInt(spread*2+1)-spread;
    		int z = r.nextInt(spread*2+1)-spread;
			int y = maxy;
			Location zombieSpawnLocation = new Location(Bukkit.getWorld("world"), x, y, z);
			zombieSpawnLocation.add(spawnLocation.getX(), 0, spawnLocation.getZ());
			continuer = true;
			while (y > 0 && continuer) {
				y--;
				zombieSpawnLocation.setY(y);
				block = zombieSpawnLocation.getBlock();
				if (!block.isEmpty()) {
					zombieSpawnLocation.setY(y+2);
					continuer = false;
				}
			}
    		// SPAWN ZOMBIE
			Bukkit.getWorld("world").spawnEntity(zombieSpawnLocation, EntityType.ZOMBIE);
    	}
	}

	public boolean isMagicChest(Location location) {
		boolean result = false;
		int x = (int)location.getX();
		int y = (int)location.getY();
		int z = (int)location.getZ();
		try
		{
			//OPENING FILE
		    File f = new File ("magicChests");
		    FileReader fr = new FileReader (f);
		    BufferedReader br = new BufferedReader (fr);
		    //NEW FILE OPENING
		    File nf = new File("tempFile");
		    
		    try
		    {
		    	PrintWriter pw = new PrintWriter (new BufferedWriter (new FileWriter (nf)));

		    	
		    	String[] params;
		        String line = br.readLine();
		        params = line.split(":");	

		        while (line != null)
		        {
		            params = line.split(":");
		            if (params[0].equals(String.valueOf(x)) && params[1].equals(String.valueOf(y)) && params[2].equals(String.valueOf(z))) {
		            	result = true;
		            }else {
		            	pw.println(line);
		            }		            
		            line = br.readLine();
		        }
		        br.close();
		        fr.close();
		        pw.close();
		        f.delete();
		        nf.renameTo(f);
		    }
		    catch (IOException exception)
		    {
		        System.out.println ("Erreur lors de la lecture du fichier MagicChests.");
		    }
		}
		catch (FileNotFoundException exception)
		{
		    System.out.println ("Le fichier MagicChests n'a pas été trouvé");
		}
		return result;
	}


	public static Entity[] getNearbyEntities(Location l, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		HashSet<Entity> radiusEntities = new HashSet<Entity>();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
				for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk()
						.getEntities()) {
					if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock() != l.getBlock())
						radiusEntities.add(e);
				}
			}
		}
		return radiusEntities.toArray(new Entity[radiusEntities.size()]);
	}

	public double min(double a, double b) {
		if (a < b) {
			return a;
		} else {
			return b;
		}
	}

	public double bord(double x, double bordure) {
		double result = x;
		if (x > bordure) {
			result = bordure;
		} else if (x < bordure*(-1)){
			result = bordure*(-1);
		}
		return result;
	}
	
	public void MJArenePlayerKill(String player) {
		try
		{
			//OPENING FILE
		    File f = new File ("MJ1_Arene");
		    FileReader fr = new FileReader (f);
		    BufferedReader br = new BufferedReader (fr);
		    //NEW FILE OPENING
		    File nf = new File("tempFile_Arene");
		    
		    try
		    {
		    	PrintWriter pw = new PrintWriter (new BufferedWriter (new FileWriter (nf)));

		    	
		    	//String params;
		        String line = br.readLine();
		        //params = line.split(":");	

		        while (line != null)
		        {
		            //params = line.split(":");
		            if (!player.equalsIgnoreCase(line)) {
		            	pw.println(line);	            
		            }
		            line = br.readLine();
		        }
		        br.close();
		        fr.close();
		        pw.close();
		        f.delete();
		        nf.renameTo(f);
		    }
		    catch (IOException exception)
		    {
		        System.out.println ("Erreur lors de la lecture du fichier MagicChests.");
		    }
		}
		catch (FileNotFoundException exception)
		{
		    System.out.println ("Le fichier MagicChests n'a pas été trouvé");
		}
	}

	public Location MJSheepVsPigMonsterLocations(int TeamNumber) {
		Location startLocation = new Location(Bukkit.getWorld("Mini_Games"), 0, 6, 0);
		Random r = new Random();
		double rx = r.nextInt(7)-3;
		double rz = r.nextInt(7)-3;
		
		switch (TeamNumber) {

		case 1:
			startLocation.setX(100 + rx);
			startLocation.setY(91);
			startLocation.setZ(25 + rz);
			break;
		case 2:
			startLocation.setX(100+ rx);
			startLocation.setY(91);
			startLocation.setZ(125 + rz);
			break;
		case 3:
			startLocation.setX(200+ rx);
			startLocation.setY(91);
			startLocation.setZ(25 + rz);
			break;
		case 4:
			startLocation.setX(200+ rx);
			startLocation.setY(91);
			startLocation.setZ(125 + rz);
			break;
		case 5:
			startLocation.setX(100+ rx);
			startLocation.setY(91);
			startLocation.setZ(225 + rz);
			break;
		case 6:
			startLocation.setX(200+ rx);
			startLocation.setY(91);
			startLocation.setZ(225 + rz);
			break;
		}

		return startLocation;
}
	
	public Location MJSheepVsPigPlayerLocations(int TeamNumber) {
		Location startLocation = new Location(Bukkit.getWorld("Mini_Games"), 0, 6, 0);
		switch (TeamNumber) {

		case 1:
			startLocation.setX(81);
			startLocation.setY(98);
			startLocation.setZ(45);
			break;
		case 2:
			startLocation.setX(81);
			startLocation.setY(98);
			startLocation.setZ(145);
			break;
		case 3:
			startLocation.setX(181);
			startLocation.setY(98);
			startLocation.setZ(45);
			break;
		case 4:
			startLocation.setX(181);
			startLocation.setY(98);
			startLocation.setZ(145);
			break;
		case 5:
			startLocation.setX(81);
			startLocation.setY(98);
			startLocation.setZ(245);
			break;
		case 6:
			startLocation.setX(181);
			startLocation.setY(98);
			startLocation.setZ(245);
			break;
		}

		return startLocation;
}

	public void SignShopClick(PlayerInteractEvent event) {

		/*          ___________________________
		 *         |          [TYPE]           |
		 *         |    [ITEM / BUFF / ...]    |
		 *         |          [PRICE]          |
		 *         |      [INFOS or W/e]       |         
		 *         |___________________________|
		 */
		Sign sign = (Sign) event.getClickedBlock().getState();
		int price = Integer.parseInt(sign.getLine(2));
		MainGame mg = new MainGame();
		int teamNumber = mg.getTeamNumberByTeamPlayer(event.getPlayer());
		int money;
		boolean bought = false;
		File f = new File("MJ2_SheepVsPig_Money");
		File f2 = new File("MJ2_SheepVsPig_Money_TEMP");
		FileWriter fw2;
		try {
			fw2 = new FileWriter(f2);
			String line;

			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			int i;
			for (i = 1; i < teamNumber; i++) {
				line = br.readLine();
				fw2.write(line);
				fw2.write("\r\n");
			}
			line = br.readLine();
			money = Integer.parseInt(line);
			//BUYING 
				//ENOUGH GOLD
			if (money >= price) {
				//GIVING ITEMS iF POSSIBLE
				bought = SignShopGiveItems(sign, event.getPlayer(), mg);
			}
			if (bought == true) {
				//SENDING MSG TO TEAM
				Player[] ps = mg.getTeambyTeamNumber(teamNumber).getmembres();
				for (Player p : ps) {
					p.sendMessage("You payed §e" + price +"g§f for [" + sign.getLine(1) + "]. You still have §e" + (money - price) + "g§f.");
				}
				//UPDATING MONEY FILE
				fw2.write(String.valueOf(money - price));
				fw2.write("\r\n");

				line = br.readLine();
				while (line != null) {
					fw2.write(line);
					fw2.write("\r\n");
					line = br.readLine();
				}
				br.close();
				fr.close();
				fw2.close();
				f.delete();
				f2.renameTo(f);
			} else {
				// NOT ENOUGH GOLD
				if (money < price) {
					event.getPlayer().sendMessage("You need more gold [§e" + money + "§f/§e" + price + "§f] !");					
				}
				br.close();
				fr.close();
				fw2.close();
				f2.delete();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean SignShopGiveItems(Sign sign, Player p, MainGame mg) {
		String type = sign.getLine(0);
		String item = sign.getLine(1);
		boolean bought = false;
		
		if (type.equalsIgnoreCase("ITEM")) {
			if (item.equalsIgnoreCase("STONE SWORD")) {
				p.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
				bought = true;
			}
			else if (item.equalsIgnoreCase("DIAMOND SWORD")) {
				p.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
				bought = true;
			}
			else if (item.equalsIgnoreCase("IRON SWORD")) {
				p.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
				bought = true;
			}
			else if (item.equalsIgnoreCase("WOOD SWORD")) {
				p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
				bought = true;
			}
			else if (item.equalsIgnoreCase("LEATHER ARMOR")) {
				p.getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));
				p.getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
				p.getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
				p.getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
				bought = true;
			}
			else if (item.equalsIgnoreCase("IRON ARMOR")) {
				p.getInventory().addItem(new ItemStack(Material.IRON_BOOTS));
				p.getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE));
				p.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS));
				p.getInventory().addItem(new ItemStack(Material.IRON_HELMET));
				bought = true;
			}
			else if (item.equalsIgnoreCase("CHAIN ARMOR")) {
				p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS));
				p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
				p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS));
				p.getInventory().addItem(new ItemStack(Material.CHAINMAIL_HELMET));
				bought = true;
			}
			else if (item.equalsIgnoreCase("DIAMOND ARMOR")) {
				p.getInventory().addItem(new ItemStack(Material.DIAMOND_BOOTS));
				p.getInventory().addItem(new ItemStack(Material.DIAMOND_CHESTPLATE));
				p.getInventory().addItem(new ItemStack(Material.DIAMOND_LEGGINGS));
				p.getInventory().addItem(new ItemStack(Material.DIAMOND_HELMET));
				bought = true;
			}
			else if (item.equalsIgnoreCase("MILK")) {
				p.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));
				bought = true;
			}
			else if (item.equalsIgnoreCase("APPLE x10	")) {
				p.getInventory().addItem(new ItemStack(Material.APPLE,10));
				bought = true;
			}
			else if (item.equalsIgnoreCase("GOLDEN APPLE")) {
				p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
				bought = true;
			}
			else if (item.equalsIgnoreCase("GOLDEN APPLE++")) {
				short data = 1;
				ItemStack is = new ItemStack(Material.GOLDEN_APPLE,data,data);
				p.getInventory().addItem(is);
				bought = true;
			}
		}
		
		//MOB LVL + 1 && INCOME + 15
		else if (type.equalsIgnoreCase("UPGRADE")){
			if (item.equalsIgnoreCase("+1 mob LvL")) {
				bought = true;
				File f1 = new File("MJ2_SheepVsPig_MobLvl");
				File f2 = new File("MJ2_SheepVsPig_MobLvl_TEMP");
				File f3 = new File("MJ2_SheepVsPig_Income");
				File f4 = new File("MJ2_SheepVsPig_Income_TEMP");
				
				int numberOfTeams = mg.getNumberOfTeams();
				int i;
				String lineMobLvl;
				String lineIncome;
				int mobLvl = 0;
				int income = 0;
				try {
					
					FileReader fr1 = new FileReader(f1);
					BufferedReader br1 = new BufferedReader(fr1);
					FileWriter fw2 = new FileWriter(f2);
					
					FileReader fr3 = new FileReader(f3);
					BufferedReader br3 = new BufferedReader(fr3);
					FileWriter fw4 = new FileWriter(f4);
								
					for (i = 1; i <= numberOfTeams; i++) {
						lineMobLvl = br1.readLine();
						lineIncome = br3.readLine();
						if (i == mg.getTeamNumberByTeamPlayer(p)) {
							mobLvl = Integer.parseInt(lineMobLvl)+1;
							lineMobLvl = String.valueOf(mobLvl);
							
							income = Integer.parseInt(lineIncome)+15;
							lineIncome = String.valueOf(income);
						}
						fw2.write(lineMobLvl);
						fw2.write("\r\n");
						
						fw4.write(lineIncome);
						fw4.write("\r\n");
					}
					fw2.close();
					fw4.close();
					br1.close();
					br3.close();
					fr1.close();
					fr3.close();
					f1.delete();
					f2.renameTo(f1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//PM UPGRADE
				Player[] ps = mg.getTeamByPlayer(p).getmembres();
				for (Player player : ps) {
					player.sendMessage("[UPGRADE] You bought a Mob LvL upgrade for a total of " + mobLvl
							+ " Mobs Lvls and earned 15 income for a total of " + income + " income.");
				}
			}	
		}else if (type.equalsIgnoreCase("SPAWN")){
			// VARIABLES
			int numberOfTeams = mg.getNumberOfTeams();
			int teamNumber = mg.getTeamNumberByTeamPlayer(p);
			int i;
			String lineCd;
			int cd;

			// CHECK CD
			File f3 = new File("MJ2_SheepVsPig_SpawnCd");
			File f4 = new File("MJ2_SheepVsPig_SpawnCd_TEMP");
			try {

				FileReader fr3 = new FileReader(f3);
				BufferedReader br3 = new BufferedReader(fr3);
				FileWriter fw4 = new FileWriter(f4);

				for (i = 1; i <= numberOfTeams; i++) {
					lineCd = br3.readLine();
					if (i == teamNumber) {
						cd = Integer.parseInt(lineCd);
						if (cd > 0) {
							bought = true;
							cd--;
						} else {
							p.sendMessage("You bought too many [Spawns] recently ! You need to wait cooldown's resets (every 15s)");
						}
						lineCd = String.valueOf(cd);

					}
					fw4.write(lineCd);
					fw4.write("\r\n");

				}
				fw4.close();
				br3.close();
				fr3.close();
				f3.delete();
				f4.renameTo(f3);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (bought == true) {

				File f1 = new File("MJ2_SheepVsPig_Income");
				File f2 = new File("MJ2_SheepVsPig_Income_TEMP");

				Random r = new Random();
				int rx = r.nextInt(numberOfTeams - 1) + 1;
				if (rx == teamNumber)
					rx = numberOfTeams;
				String lineIncome;
				int bonusIncome = 0;
				int income = 0;
				// SPAWN MONSTERS
				if (item.equalsIgnoreCase("VILLAGER")) {
					bonusIncome = 3;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Villager.class);
				} else if (item.equalsIgnoreCase("ZOMBIE")) {
					bonusIncome = 5;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Zombie.class);
				} else if (item.equalsIgnoreCase("WOLF x3")) {
					bonusIncome = 25;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Wolf.class);
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Wolf.class);
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Wolf.class);
				} else if (item.equalsIgnoreCase("SKELETON x2")) {
					bonusIncome = 37;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Skeleton.class);
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Skeleton.class);
				} else if (item.equalsIgnoreCase("SLIME")) {
					bonusIncome = 62;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Slime.class);
				} else if (item.equalsIgnoreCase("WITCH")) {
					bonusIncome = 125;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Witch.class);
				} else if (item.equalsIgnoreCase("BLAZE")) {
					bonusIncome = 250;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Blaze.class);
				} else if (item.equalsIgnoreCase("GHAST")) {
					bonusIncome = 375;
					Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(rx), Ghast.class);
				}

				// INCOME UPDATE
				try {

					FileReader fr1 = new FileReader(f1);
					BufferedReader br1 = new BufferedReader(fr1);
					FileWriter fw2 = new FileWriter(f2);

					for (i = 1; i <= numberOfTeams; i++) {
						lineIncome = br1.readLine();
						if (i == teamNumber) {
							income = Integer.parseInt(lineIncome) + bonusIncome;
							lineIncome = String.valueOf(income);

						}
						fw2.write(lineIncome);
						fw2.write("\r\n");

					}
					fw2.close();
					br1.close();
					fr1.close();
					f1.delete();
					f2.renameTo(f1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// PM UPGRADE
				Player[] ps = mg.getTeamByPlayer(p).getmembres();
				for (Player player : ps) {
					player.sendMessage("[UPGRADE] You bought a Spawn and earned §e" + bonusIncome
							+ "g§f income for a total of §e" + income + "g§f income.");
				}
			}
		}
		return bought;
	}

	public void spawnMonster(int teamNumber, MainGame mg) {
		//CALCUL MOB LVL
		File f1 = new File("MJ2_SheepVsPig_MobLvl");
		int numberOfTeams = mg.getNumberOfTeams();
		int i;
		int mobLvl = 0;
		String line;
		try {
			FileReader fr1 = new FileReader(f1);
			BufferedReader br1 = new BufferedReader(fr1);
			
			for (i = 1; i <= numberOfTeams; i++) {
				line = br1.readLine();
				if (i != teamNumber) {
					mobLvl = mobLvl + Integer.parseInt(line);
				}
			}
			br1.close();
			fr1.close();
			//SPAWN MONSTERS
			if (mobLvl > 50*(numberOfTeams-1)){
				//WITHER SKELETON
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), WitherSkeleton.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), WitherSkeleton.class);
			} else if (mobLvl > 32*(numberOfTeams-1)){
				//CAVE SPIDER
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), CaveSpider.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), CaveSpider.class);
			} else if (mobLvl > 26*(numberOfTeams-1)){
				//PIGMAN
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), PigZombie.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), PigZombie.class);
			} else if (mobLvl > 18*(numberOfTeams-1)){
				//ZOMBIE
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Zombie.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Zombie.class);
			} else if (mobLvl > 13*(numberOfTeams-1)){
				//SPIDER
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Spider.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Spider.class);
			} else if (mobLvl > 9*(numberOfTeams-1)){
				//POLAR BEAR
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), PolarBear.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), PolarBear.class);
			} else if (mobLvl > 6*(numberOfTeams-1)){
				//LLAMA
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Llama.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Llama.class);
			} else if (mobLvl > 4*(numberOfTeams-1)){
				//VILLAGEOIS
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Villager.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Villager.class);
			} else if (mobLvl > 2*(numberOfTeams-1)){
				//COW
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Cow.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Cow.class);
			} else if (mobLvl > 1*(numberOfTeams-1)){
				//SHEEP
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Sheep.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Sheep.class);
			} else {
				//CHICKEN
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Chicken.class);
				Bukkit.getWorld("Mini_Games").spawn(MJSheepVsPigMonsterLocations(teamNumber), Chicken.class);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void MJSheepVsPigOnEntityDeath(MainGame mg, EntityDeathEvent event) {
		int numberOfTeams = mg.getNumberOfTeams();
		int killerTeamNumber = mg.getTeamNumberByTeamPlayer(event.getEntity().getKiller());
		System.out.println("KILLER TEAM : " + killerTeamNumber);
		
		//REWARD
		int reward = MjSheepVsPigEntityReward(event);
		File f = new File("MJ2_SheepVsPig_Money");
		File f2 = new File("MJ2_SheepVsPig_Money_TEMP");
		try {
			FileWriter fw2 = new FileWriter(f2);
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line;
			int money;
			int i;

			for (i = 1; i <= numberOfTeams; i++) {
				line = br.readLine();
				if (i != killerTeamNumber) {
					spawnMonster(i, mg);
				} else {
					money = Integer.parseInt(line) + reward;
					line = String.valueOf(money);
					Player[] ps = mg.getTeambyTeamNumber(i).getmembres();
					for (Player p : ps) {
						p.sendMessage("[REWARD]You Earn §e"+ reward +"g§f for a total of §e" + money + "g§f.");
					}
				}
				fw2.write(line);
				fw2.write("\r\n");
			}
			br.close();
			fr.close();
			fw2.close();
			f.delete();
			f2.renameTo(f);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int MjSheepVsPigEntityReward(EntityDeathEvent event) {
		int reward = 0;
		if (event.getEntityType() == EntityType.CHICKEN) {
			reward = 2;
		} else if (event.getEntityType() == EntityType.SHEEP) {
			reward = 4;
		} else if (event.getEntityType() == EntityType.COW) {
			reward = 6;
		} else if (event.getEntityType() == EntityType.VILLAGER) {
			reward = 8;
		} else if (event.getEntityType() == EntityType.LLAMA) {
			reward = 10;
		} else if (event.getEntityType() == EntityType.POLAR_BEAR) {
			reward = 12;
		} else if (event.getEntityType() == EntityType.SPIDER) {
			reward = 14;
		} else if (event.getEntityType() == EntityType.ZOMBIE) {
			reward = 16;
		} else if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
			reward = 18;
		} else if (event.getEntityType() == EntityType.CAVE_SPIDER) {
			reward = 20;
		} else if (event.getEntityType() == EntityType.WITHER_SKELETON) {
			reward = 22;
		} else if (event.getEntityType() == EntityType.WOLF) {
			reward = 15;
		} else if (event.getEntityType() == EntityType.SKELETON) {
			reward = 25;
		} else if (event.getEntityType() == EntityType.SLIME) {
			reward = 5;
		} else if (event.getEntityType() == EntityType.WITCH) {
			reward = 150;
		} else if (event.getEntityType() == EntityType.BLAZE) {
			reward = 300;
		} else if (event.getEntityType() == EntityType.GHAST) {
			reward = 450;
		}
		
		return reward;
	}

	public void MJStompThemAllPlayerLost(Player p) {
		String pn = p.getName();
		String[] pns;
		File f = new File ("MJ3_StompThemAll");
		File ft = new File ("MJ3_StompThemAll_Temp");
		
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			FileWriter fwt = new FileWriter(ft);
			
			pns = br.readLine().split(":");
			for (String names : pns) {
				if (!names.equalsIgnoreCase(pn)) {
					fwt.write(names);
					fwt.write(":");
				}
			}
			fwt.close();
			br.close();
			fr.close();
			f.delete();
			ft.renameTo(f);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}







