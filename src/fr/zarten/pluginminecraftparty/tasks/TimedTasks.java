package fr.zarten.pluginminecraftparty.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import fr.zarten.pluginminecraftparty.MainGame;
import fr.zarten.pluginminecraftparty.Equipe;

public class TimedTasks extends BukkitRunnable {

	private int timer = 1800;
	//1200 : 20min
	int WorldSize = 400;
	int mj = 0;
	int state = -1; // 0 :INI / 1 : GAME / 2 : RESET
	long timesaved;
	MainGame mg;
	Player[] ps;
	Location[] playerLocations;
	String[][] inventories;
	double[] healths;
	int[] foods;
	int[] experiences;
	//WORLDS
	@SuppressWarnings("unused")
	private World world;
	//SCOREBOARD
	ScoreboardManager manager = Bukkit.getScoreboardManager();
	Scoreboard board = manager.getNewScoreboard();
	ArrayList<Team> teams = new ArrayList<Team>();
	Objective tabObjective;
	Objective scoreObjective;
	Score[] score;

	
	//Arene
	String winner = null;
	
	//Sheep Vs Pig
	int numberOfTeams;
	int maxEntities = 500;
	String maxCd = "7";
	int cd = 15;  
	String startingGold = "25";
	
	//StompThemAll
	int tpTimer = 30;
	
	//@SuppressWarnings("deprecation")
	@Override
	public void run() {
		if (timer % 120 == 0 & timer != 0) {
			//Bukkit.broadcastMessage("Minijeu dans  " + timer/60 + " minute(s) !");
			Bukkit.broadcastMessage("Coffre Magique dans  " + timer/60 + " minute(s) !");
		}
		//CHEST MODE 
		if(timer == 0) {
			//POSITION DU COFFRE
			Random r = new Random();
			double x = r.nextInt(WorldSize)-(WorldSize/2);
			double z = r.nextInt(WorldSize)-(WorldSize/2);
			double maxy = Bukkit.getWorld("world").getMaxHeight();
			double y = maxy;
			Location chestLocation = new Location(Bukkit.getWorld("world"), x, y, z);
			Block block;
			while (y > 0) {
				y--;
				chestLocation.setY(y);
				block = chestLocation.getBlock();
				if (!block.isEmpty() && block.getType() != Material.LEAVES) {
					break;
				}
			}
			Bukkit.broadcastMessage("Un coffre magique est apparu en x = " + x + " y(hauteur) = " + y + " z = " + z);
			//CREATION DE LA TOUR
			this.BuildTower(x, y, z);
			
			//CHEST
			this.CreateChest(x, y, z);
			
			
			//RESET
			timer = 1800;
		}
		timer--;
		//MINIGAME MODE
		/*if (timer == 0) {
			if (state == -1) {
				mg = new MainGame();
				int j = 0;
				for (Equipe t : mg.getGameteams()) {
					// SCOREBOARD TEAMS
					teams.add(board.registerNewTeam(t.getName()));
					ps = t.getmembres();
					for (Player x : ps) {
						// SCOREBOARD PLAYERS
						teams.get(j).addPlayer(x);
						x.setScoreboard(board);
					}
					j++;
				}
				state = 0;
			}
			//STATE 0
			if (state == 0) {
				//SETTING WORLD FROM "MINI_GAMES_ORIGINAL"
				copyWorld();
				//HOLOGRAMS :
				MJSheepVsPigHolograms(mg);
				// CHOIX MINIJEU :
				winner = null;
				//Location miniJeuLocation = new Location(Bukkit.getWorld("Mini_Games"), 0, 5, 0);
				Random r = new Random();
				mj = r.nextInt(1); // 1 : ArenePvP // 2 : Sheeps vs Pigs
				mj++;
				mj = 2;

				// SAVE
					// TIME
				timesaved = Bukkit.getWorld("world").getTime();
					// PLAYERS
				mg = new MainGame();
				int playerNb = mg.getPlayerNumber();
				
				playerLocations = new Location[playerNb];
				inventories = new String[playerNb][];
				healths = new double[playerNb];
				foods = new int[playerNb];
				experiences = new int[playerNb];
				
				int i = 0;
				for (Equipe t : mg.getGameteams()) {
					ps = t.getmembres();
					for (Player x : ps) {
						inventories[i] = playerInventoryToBase64(x.getInventory());
						System.out.println(inventories[i][1]);
						x.getInventory().clear();
						x.updateInventory();
						playerLocations[i] = x.getLocation();
						healths[i] = x.getHealth();
						x.setHealth(x.getMaxHealth());
						foods[i] = x.getFoodLevel();
						x.setFoodLevel(20);
						experiences[i] = x.getTotalExperience();
						i++;
					}
				}
					//INI MINI JEU
				if (mj == 1) {
					MJARenePvPINI(mg);
				}
				else if (mj == 2) {
					MJSheepVsPigINI(mg);
				}
				else if (mj == 3) {
					MJStompThemAllINI(mg);
				}
				state = 1;
			//STATE 1	
			} else if (state == 1) {

				// JEU
				if (mj == 1) {
					winner = MJArenePvP(mg);
				}
				else if (mj == 2) {
					winner = MJSheepVsPig(mg);
				}
				else if (mj == 3){
					winner = MJStompThemAll(mg);
				}
				if (winner != null) {
					Bukkit.broadcastMessage("GAGNANT : " + winner);
					state = 2;					
				}
			//STATE 2
			} else if (state == 2) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// SET BACK
				// TIME
				Bukkit.getWorld("world").setTime(timesaved);
				// PLAYERS
				int i = 0;
				
				for (Equipe t : mg.getGameteams()) {
					ps = t.getmembres();
					for (Player x : ps) {
						// RECLEAR INVENTORY
						x.getInventory().clear();
						// SETTING BACK INVENTORY
						try {

							x.getInventory().setContents(fromBase64(inventories[i][0], x.getInventory()).getContents());
							x.getInventory().setArmorContents(itemStackArrayFromBase64(inventories[i][1]));
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						x.teleport(playerLocations[i]);
						x.setGameMode(GameMode.SURVIVAL);
						x.setHealth(healths[i]);
						x.setFoodLevel(foods[i]);
						x.setTotalExperience(experiences[i]);
						if (x.getName().equalsIgnoreCase(winner)){
							MJReward(x);
						}
						i++;
					}
				}
				// GIVE BONUS

				// RESET TIMER
				state = 0;
				timer = 180;
				//RESET WORLDS
				deleteWorld();
			}
			
		} else {
			timer--;
		}*/
	}
	
	@SuppressWarnings("deprecation")
	public void BuildTower(double x, double y, double z) {
		int i;
		Location towerLocation = new Location(Bukkit.getWorld("world"), x, y, z);
		for (i = 0; i < 10; i++) {
			
			//OAK WOOD
			towerLocation.setX(x-1);
			towerLocation.setZ(z-1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x-1);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z-1);
			towerLocation.getBlock().setType(Material.LOG);
			
			//PLANCHE
			towerLocation.setX(x+1);
			towerLocation.setZ(z);
			towerLocation.getBlock().setType(Material.WOOD);
			
			towerLocation.setX(x-1);
			towerLocation.setZ(z);
			towerLocation.getBlock().setType(Material.WOOD);
			
			towerLocation.setX(x);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.WOOD);

			
			//BEDROCK
			towerLocation.setX(x);
			towerLocation.setZ(z);
			towerLocation.getBlock().setType(Material.BEDROCK);
			towerLocation.setY(i+y);
		}
		for (i=9; i<12;i++) {
			//OAK WOOD+
			towerLocation.setX(x-1);
			towerLocation.setZ(z-1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x-1);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z-1);
			towerLocation.getBlock().setType(Material.LOG);
			
			towerLocation.setY(i+y);

		}
		//TOIT
		towerLocation.setX(x-1);
		towerLocation.setZ(z-1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)8);
		
		towerLocation.setX(x-1);
		towerLocation.setZ(z);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)8);
		
		towerLocation.setX(x-1);
		towerLocation.setZ(z+1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)4);
		
		towerLocation.setX(x+1);
		towerLocation.setZ(z-1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)4);
		
		towerLocation.setX(x+1);
		towerLocation.setZ(z);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)8);
		
		towerLocation.setX(x+1);
		towerLocation.setZ(z+1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)8);
		
		towerLocation.setX(x);
		towerLocation.setZ(z+1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)4);
		
		
		towerLocation.setX(x);
		towerLocation.setZ(z-1);
		towerLocation.getBlock().setType(Material.LOG);
		towerLocation.getBlock().setData((byte)4);
		
		//DEPASSEMENT
		for (i = 0; i<2;i++) {
			towerLocation.setX(x-1);
			towerLocation.setZ(z-2);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)8);
			
			towerLocation.setX(x-2);
			towerLocation.setZ(z-1);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)4);
			
			towerLocation.setX(x-1);
			towerLocation.setZ(z+2);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)8);
			
			towerLocation.setX(x-2);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)4);
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z+2);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)8);
			
			towerLocation.setX(x+2);
			towerLocation.setZ(z+1);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)4);
			
			towerLocation.setX(x+2);
			towerLocation.setZ(z+-1);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)4);
			
			
			towerLocation.setX(x+1);
			towerLocation.setZ(z-2);
			towerLocation.getBlock().setType(Material.LOG);
			towerLocation.getBlock().setData((byte)8);
			
			towerLocation.setY(y+8);
		}
	}

	public void CreateChest(double x, double y, double z) {
		//FILE
		File file = new File("magicChests");
		try {
			FileWriter fw = new FileWriter (file, true);
			fw.write (String.valueOf((int)x));
			fw.write(":");
			fw.write (String.valueOf((int)y+9));
			fw.write(":");
			fw.write (String.valueOf((int)z));
			fw.write("\r\n");
			fw.close();
		} catch (IOException e) {
			System.out.println ("Erreur lors de la lecture du fichier magicChests ");
			e.printStackTrace();
		}
		
		//LOCATION
		Location chestLocation = new Location(Bukkit.getWorld("world"), x, y+9, z);
		
		//CHEST
		chestLocation.getBlock().setType(Material.CHEST);
		
		//CHEST INVENTORY
		Chest chest = (Chest) chestLocation.getBlock().getState();
		Inventory chestInventory = chest.getInventory();
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());		
	}
	
	public ItemStack RandomItem() {
		Random r = new Random();
		int x = r.nextInt(61);
		ItemStack item;

		//COMMON ITEMS :
		if (x == 0 || x == 1 || x == 2 ) {
			item = new ItemStack(Material.ARROW,16);
		}
		else if (x == 3 || x == 4 || x == 5 ) {
			item = new ItemStack(Material.GOLD_INGOT,8);
		}
		else if (x == 6 || x == 7 || x == 8 ) {
			item = new ItemStack(Material.IRON_INGOT,12);
		}
		else if (x == 9 || x == 10 || x == 11 ) {
			item = new ItemStack(Material.IRON_SWORD,1);
		}
		else if (x == 12 || x == 13 || x == 14 ) {
			item = new ItemStack(Material.BOW,1);
		}
		else if (x == 15 || x == 16 || x == 17 ) {
			item = new ItemStack(Material.REDSTONE,32);
		}
		else if (x == 18 || x == 19 || x == 20 ) {
			item = new ItemStack(Material.SNOW_BALL,3);
			item.getItemMeta().setDisplayName("Balle en neige lourde");
			item.getItemMeta().setLore(Arrays.asList("Je suis magique !","Lancez moi !"));	
		}
		else if (x == 21 || x == 22 || x == 23 ) {
			item = new ItemStack(Material.MELON_SEEDS,4);
		}
		else if (x == 24 || x == 25 || x == 26 ) {
			item = new ItemStack(Material.OBSIDIAN,1);
		}
		else if (x == 27 || x == 28 || x == 29 ) {
			item = new ItemStack(Material.EXP_BOTTLE,4);
		}
		else if (x == 30 || x == 31 || x == 32 ) {
			item = new ItemStack(Material.IRON_CHESTPLATE,1);
		}
		else if (x == 33 || x == 34 || x == 35 ) {
			item = new ItemStack(Material.IRON_BOOTS,1);
		}
		else if (x == 36 || x == 37 || x == 38 ) {
			item = new ItemStack(Material.IRON_HELMET,1);
		}
		else if (x == 39 || x == 40 || x == 41 ) {
			item = new ItemStack(Material.IRON_LEGGINGS,1);
		} 
		
		//RARE ITEMS :
		else if (x == 42 || x == 43 ) {
			item = new ItemStack(Material.MONSTER_EGG,2,(byte)90);
		} 
		else if (x == 44 || x == 45 ) {
			item = new ItemStack(Material.ARROW,6);
			//datatype instant heal arrow
		} 
		else if (x == 46 || x == 47 ) {
			item = new ItemStack(Material.DIAMOND_AXE,1);
		} 
		else if (x == 48 || x == 49 ) {
			item = new ItemStack(Material.DIAMOND_SPADE,1);
		} 
		else if (x == 50 || x == 51 ) {
			item = new ItemStack(Material.DIAMOND_SWORD,1);
		} 
		else if (x == 52 || x == 53 ) {
			item = new ItemStack(Material.TNT,6);
		} 
		
		//EPIC ITEMS
		 
		else if (x == 54 ) {
			item = new ItemStack(Material.MONSTER_EGG,2,(byte)95);
		}
		else if (x == 55 || x == 56) {
			item = new ItemStack(Material.TOTEM,1);
		}
		//else if (x == 56 ) {
			//item = new ItemStack(Material.POTION,2);
			//Datatype Invisibility
		//}
		else if (x == 57 ) {
			item = new ItemStack(Material.ENCHANTMENT_TABLE,1);
		}
		else if (x == 58 || x == 54) {
			item = new ItemStack(Material.DIAMOND,2);
		}
		else if (x == 59 ) {
			item = new ItemStack(Material.PISTON_STICKY_BASE,24);
		}
		else {
			item = new ItemStack(Material.ELYTRA,1);
		}
		
		return item;
		}

	 /**
     * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
     * 
     * @param playerInventory to turn into an array of strings.
     * @return Array of strings: [ main content, armor content ]
     * @throws IllegalStateException
     */
    public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
    	//get the main content part, this doesn't return the armor
    	String content = toBase64(playerInventory);
    	String armor = itemStackArrayToBase64(playerInventory.getArmorContents());
    	
    	return new String[] { content, armor };
    }
    
    /**
     * 
     * A method to serialize an {@link ItemStack} array to Base64 String.
     * 
     * <p />
     * 
     * Based off of {@link #toBase64(Inventory)}.
     * 
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException
     */
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
    	try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // Write the size of the inventory
            dataOutput.writeInt(items.length);
            
            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    
    /**
     * A method to serialize an inventory to Base64 string.
     * 
     * <p />
     * 
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     * 
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     * 
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException
     */
    public static String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // Write the size of the inventory
            dataOutput.writeInt(inventory.getSize());
            
            // Save every element in the list
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    
    /**
     * 
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     * 
     * <p />
     * 
     * Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.
     * 
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     * 
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     * @throws IOException
     */

	public static Inventory fromBase64(String data, PlayerInventory inventory) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            // Read the serialized inventory
            int InvSize = dataInput.readInt();
			if (InvSize == inventory.getSize()) {

				for (int i = 0; i < inventory.getSize(); i++) {
					inventory.setItem(i, (ItemStack) dataInput.readObject());
				}
			}else {
				System.out.println("Tailles d'inventaires Incompatibles");
			}
            
            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
    
    /**
     * Gets an array of ItemStacks from Base64 string.
     * 
     * <p />
     * 
     * Base off of {@link #fromBase64(String)}.
     * 
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
    	try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
            	items[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    
    //MJ = 1
    @SuppressWarnings("deprecation")
	public void MJARenePvPINI(MainGame mg) {
    	MJArenePvPInfos(); 
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	// INITIALISATION
		Bukkit.getWorld("Mini_Games").setDifficulty(Difficulty.HARD);
		Location ArenaLocation = new Location(Bukkit.getWorld("Mini_Games"), 4, 4, 64);
		//Bukkit.getWorld("Mini_Games").setSpawnLocation(SpectatorLocation);
		
		// A REVOIR
		Entity[] ett = getNearbyEntities(ArenaLocation, 50); 
		for (Entity e : ett) {
			e.remove();
		}
		Player[] ps;
		int i = 0;
		int j = 0;

		File f = new File("MJ1_Arene");
		try {
			FileWriter fr = new FileWriter(f);

			for (Equipe t : mg.getGameteams()) {
				ps = t.getmembres();
				for (Player x : ps) {
					fr.write(x.getName());
					fr.write("\r\n");
					x.getInventory().setContents(ArenePvPInventory(x.getInventory(),j).getContents());
					x.teleport(ArenePvPLocations(i));
					x.setGameMode(GameMode.ADVENTURE);
					x.setHealth(x.getMaxHealth());
					j++;
					i++;

				}
				j = 0;
				fr.write("\r\n");
			}
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
    
	public String MJArenePvP(MainGame mg) {
		// DETERMINATION DU GAGANT
		String winner = null;
		File f = new File("MJ1_Arene");
		FileReader fr;
		try {
			fr = new FileReader(f);

			BufferedReader br = new BufferedReader(fr);

			String line1;
			String line2;
			boolean isLastTeam = true;

			line1 = br.readLine();

			while (line1.isEmpty()) {
				line1 = br.readLine();
			}
			System.out.println("LINE 1 :" + line1);
			// 1 = premier pseudo

			line2 = line1;
			line1 = br.readLine();

			while (!line1.isEmpty()) {
				line2 = line1;
				line1 = br.readLine();
			}
			// 2 = dernier pseudo equipe 1
			// 1 = empty
			
			while (line1 != null) {
				if (!line1.isEmpty()) {
					isLastTeam = false;
				}
				line1 = br.readLine();
			}
			
			br.close();
			fr.close();
			if (isLastTeam) {
				winner = line2;
				f.delete();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return winner;
	}

	public void MJSheepVsPigINI(MainGame mg) {
		MJSheepVsPigInfos();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// INITIALISATION				
				Bukkit.getWorld("Mini_Games").setDifficulty(Difficulty.HARD);
				Player[] ps;
				int i = 0;
				Location startPoint;
				
				//SCOREBOARD SETTING

				//KILLS
				tabObjective = board.registerNewObjective("Kills", "stat.killEntity.Sheep");
				tabObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
				tabObjective.setDisplayName(ChatColor.GREEN + "Kills");
				
				//ENTITIES
				scoreObjective = board.registerNewObjective("Monsters", "dummy");
				scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
				scoreObjective.setDisplayName(ChatColor.RED + "Number Of entities : ");
				score = new Score[mg.getNumberOfTeams()];
				
				File f = new File("MJ2_SheepVsPig");
				File f2 = new File("MJ2_SheepVsPig_Money");
				File f3 = new File("MJ2_SheepVsPig_Income");
				File f4 = new File("MJ2_SheepVsPig_MobLvl");
				File f5 = new File("MJ2_SheepVsPig_SpawnCd");
				try {
					FileWriter fr = new FileWriter(f);
					FileWriter fr2 = new FileWriter(f2);
					FileWriter fr3 = new FileWriter(f3);
					FileWriter fr4 = new FileWriter(f4);
					FileWriter fr5 = new FileWriter(f5);
					
					int j = 0;
					for (Equipe t : mg.getGameteams()) {
						//sheeps :
						score[j] = scoreObjective.getScore(ChatColor.YELLOW + t.getName());
						ps = t.getmembres();
						i++;
						startPoint = MJSheepVsPigMonsterLocations(i);
						Entity[] ett = getNearbyEntities(startPoint, 50);
						for (Entity e : ett) {
							e.remove();
						}
						for (Player x : ps) {
							
							fr.write(x.getName());
							fr.write(":");
							x.getInventory().setContents(MJSheepVsPigInventory(x.getInventory()).getContents());
							x.teleport(startPoint);
							Bukkit.getPlayer(x.getName()).setGameMode(GameMode.ADVENTURE);

						}
						j++;
						Bukkit.getWorld("Mini_Games").spawn(startPoint,Chicken.class);
						fr.write("\r\n");

						fr2.write(startingGold); //STARTING MONEY
						fr2.write("\r\n");
						
						fr3.write("0"); //STARTING INCOME
						fr3.write("\r\n");
						
						fr4.write("0"); //MOBLVL SET
						fr4.write("\r\n");
						
						fr5.write("10"); //SPAWN CD SET
						fr5.write("\r\n");
					}
					
					
					numberOfTeams = i;
					fr.close();
					fr2.close();
					fr3.close();
					fr4.close();
					fr5.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		    }
	
	@SuppressWarnings("deprecation")
	public String MJSheepVsPig(MainGame mg) {
		String result = null;
		List<String> liste = new ArrayList<String>();
		int i;
		int ett;
		cd = cd - 1;
		File f = new File("MJ2_SheepVsPig");
		File f1 = new File("MJ2_SheepVsPig_Money");
		File f2 = new File("MJ2_SheepVsPig_Income");
		File f3 = new File("MJ2_SheepVsPig_Money_TEMP");
		File f4 = new File("MJ2_SheepVsPig_SpawnCd");
		File f5 = new File("MJ2_SheepVsPig_MobLvl");

		try {
			FileReader fr2 = new FileReader(f2);
			FileReader fr5 = new FileReader(f5);
			BufferedReader br2 = new BufferedReader(fr2);
			BufferedReader br5 = new BufferedReader(fr5);
			for (i = 1; i <= numberOfTeams; i++) {
				ett = getNearbyEntitiesNumber(MJSheepVsPigMonsterLocations(i), 50);
				// SCORE TABBLE 1 : ENTITES
				if (cd > 10 || cd == 0) {
					score[i - 1].setScore(ett);
				} else if ( cd > 5) {
					// SCORE TABBLE 2 : INCOME
					score[i - 1].setScore(Integer.parseInt(br2.readLine()));
				} else {
					// SCORE TABBLE 5 : MobLvl
					score[i - 1].setScore(Integer.parseInt(br5.readLine()));
				}
				
				//Kill creepers
				Entity[] creepers = getNearbyEntities(MJSheepVsPigMonsterLocations(i), 50);
				for (Entity c : creepers) {
					if (c.getType() == EntityType.CREEPER) {
						c.remove();
					}
				}
				
				if (ett < maxEntities) {
					// TEAMS STILL IN THE GAME
					liste.add(mg.getTeamLeaderByTeamNumber(i - 1));
				}
			}
			br2.close();
			br5.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//FONTAINE DE VIE :
		if ((cd+1)%2 == 0) {
			for (Equipe t : mg.getGameteams()) {
				for (Player p : t.getmembres()) {
					for (i = 1; i <= numberOfTeams; i++) {
						if (p.getLocation().distance(MJSheepVsPigPlayerLocations(i)) < 3){
							if (p.getHealth()+2 > p.getMaxHealth()) {
								p.setHealth(p.getMaxHealth());
							} else {
								p.setHealth(p.getHealth()+2);							
							}
						}
					}
				}
			}
		}
		//ROULEMENT DES AFFICHAGES DES SCORES
		if (cd == 5) {
			scoreObjective.setDisplayName(ChatColor.RED + "Mobs Lvls bought : ");
		}
		else if (cd == 10) {
			scoreObjective.setDisplayName(ChatColor.RED + "Incomes/15s : ");
		}
		else if (cd == 0) {
			//SET SCORE 1
			scoreObjective.setDisplayName(ChatColor.RED + "Number Of entities : ");
			// ADD INCOME
			try {
				FileReader fr1 = new FileReader(f1);
				FileReader fr2 = new FileReader(f2);
				FileWriter fw3 = new FileWriter(f3);
				FileWriter fw4 = new FileWriter(f4);
				BufferedReader br1 = new BufferedReader(fr1);
				BufferedReader br2 = new BufferedReader(fr2);
				String lineMoney;
				String lineIncome;
				int money;
				int income;
				
				for (Equipe t : mg.getGameteams()) {
					//ADD INCOME
					lineMoney = br1.readLine();
					lineIncome = br2.readLine();
					income = Integer.parseInt(lineIncome);
					money = Integer.parseInt(lineMoney);
					fw3.write(String.valueOf(income + money));
					fw3.write("\r\n");
					//PM INCOME
					Player[] ps = t.getmembres();
					for (Player p : ps) {
						p.sendMessage("[INCOME] You earned §e" + income + "g§f for a total of §e" + (money+income) + "g§f.");
					}
					//CD RESET 
					fw4.write(maxCd);
					fw4.write("\r\n");
				}
				br1.close();
				br2.close();
				fr1.close();
				fr2.close();
				fw3.close();
				fw4.close();
				f1.delete();
				f3.renameTo(f1);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			// RESET
			cd = 15;
			Bukkit.getWorld("Mini_Games").setTime(20000);
		}
		if (liste.size() == 1) {
			
			result = liste.get(0);
			cd = 15;
			tabObjective.unregister();
			scoreObjective.unregister();
			f.delete();
			f1.delete();
			f2.delete();
			f4.delete();
			f5.delete();
		}

		return result;
	}
	
	public void MJStompThemAllINI(MainGame mg) {
		MJStompThemAllInfos();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// INITIALISATION
		Bukkit.getWorld("Mini_Games").setDifficulty(Difficulty.HARD);
		Bukkit.getWorld("Mini_Games").setTime(0);
		Player[] ps;
		int i = 0;
		int j = 0;
		Location startPoint = new Location(Bukkit.getWorld("Mini_Games"), -10, 5, 16);

		// SCOREBOARD SETTING
		// HEALTH SCORE
		scoreObjective = board.registerNewObjective("Health", "dummy");
		scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		scoreObjective.setDisplayName(ChatColor.RED + "Heatlh : ");
		score = new Score[mg.getPlayerNumber()];

		File f = new File("MJ3_StompThemAll");
		try {
			FileWriter fw = new FileWriter(f);

			Entity[] ett = getNearbyEntities(startPoint, 90);
			for (Entity e : ett) {
				e.remove();
			}
			for (Equipe t : mg.getGameteams()) {
				ps = t.getmembres();
				i++;
				for (Player x : ps) {
					System.out.println("AAAAAA : " + x.getName());
					fw.write(x.getName());
					fw.write(":");
					score[j] = scoreObjective.getScore(ChatColor.YELLOW + x.getName());
					score[j].setScore((int) x.getHealth());
					startPoint = MJStompThemAllLocation(i);
					x.teleport(startPoint);
					Bukkit.getPlayer(x.getName()).setGameMode(GameMode.ADVENTURE);
					j++;
				}
			}
			fw.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public String MJStompThemAll(MainGame mg) {
		tpTimer--;
		String winner = null;
		String line;
		String[] playersNames;

		File f = new File ("MJ3_StompThemAll");

		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			line = br.readLine();
			playersNames = line.split(":");
			if (playersNames.length == 1) {
				winner = playersNames[0];
			} else {
				int i = 1;
					for (String sp : playersNames) {
						score[i-1].setScore((int) mg.getPlayerbyName(sp).getHealth());
						if (tpTimer == 0) {
						Bukkit.getPlayer(sp).teleport(MJStompThemAllLocation(i));
						tpTimer = 30; 
						}
						i++;
				}
			}
			br.close();
			fr.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (winner != null) {
			tabObjective.unregister();
			scoreObjective.unregister();
			f.delete();
		}
		return winner;
	}
	
    @SuppressWarnings("deprecation")
	public PlayerInventory ArenePvPInventory(PlayerInventory p, int parite) {
    	
    	if (parite%2 == 0) {
    		p.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    		p.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
    		p.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    		p.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    		p.setItemInHand(new ItemStack(Material.STONE_AXE));
    		p.setItemInOffHand(new ItemStack(Material.SHIELD));
    	}else {
    		p.setBoots(new ItemStack(Material.LEATHER_BOOTS));
    		p.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
    		p.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
    		p.setHelmet(new ItemStack(Material.LEATHER_HELMET));
    		p.setItemInHand(new ItemStack(Material.IRON_SWORD));
    		ItemStack bow = new ItemStack(Material.BOW);
    		bow.getItemMeta().addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
    		bow.getItemMeta().addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
    		p.addItem(new ItemStack(Material.ARROW,20));
    		
    	}
    	return p;
    }
    
    public Location ArenePvPLocations(int i) {
    	Location L;
    	
		switch (i) {
		case 1:
			L = new Location(Bukkit.getWorld("Mini_Games"),0,5,80);
			break;
		case 2:
			L = new Location(Bukkit.getWorld("Mini_Games"),5,5,81);
			break;
		case 3:
			L = new Location(Bukkit.getWorld("Mini_Games"),17,5,62);
			break;
		case 4:
			L = new Location(Bukkit.getWorld("Mini_Games"),14,5,57);
			break;
		case 5:
			L = new Location(Bukkit.getWorld("Mini_Games"),-8,5,62);
			break;
		case 6:
			L = new Location(Bukkit.getWorld("Mini_Games"),-9,5,67);
			break;
		case 7:
			L = new Location(Bukkit.getWorld("Mini_Games"),9,5,80);
			break;
		case 8:
			L = new Location(Bukkit.getWorld("Mini_Games"),13,5,78);
			break;
		case 9:
			L = new Location(Bukkit.getWorld("Mini_Games"),9,5,55);
			break;
		case 10:
			L = new Location(Bukkit.getWorld("Mini_Games"),4,5,53);
			break;
		case 11:
			L = new Location(Bukkit.getWorld("Mini_Games"),-8,5,72);
			break;
		case 12:
			L = new Location(Bukkit.getWorld("Mini_Games"),-5,5,77);
			break;
		case 13:
			L = new Location(Bukkit.getWorld("Mini_Games"),17,5,72);
			break;
		case 14:
			L = new Location(Bukkit.getWorld("Mini_Games"),18,5,67);
			break;
		case 15:
			L = new Location(Bukkit.getWorld("Mini_Games"),0,5,54);
			break;
		default:
			L = new Location(Bukkit.getWorld("Mini_Games"),-5,5,57);
			break;
		}
    	return L;
    }

	@SuppressWarnings("deprecation")
	public PlayerInventory MJSheepVsPigInventory(PlayerInventory inventory) {
		inventory.setItemInHand(new ItemStack(Material.WOOD_SWORD));
		inventory.addItem(new ItemStack(Material.WOOD_SWORD,1));
		inventory.addItem(new ItemStack(Material.WOOD_SWORD,1));
		inventory.addItem(new ItemStack(Material.WOOD_SWORD,1));
		inventory.addItem(new ItemStack(Material.APPLE,5));
		
		return inventory;
	}
	
	public Location MJSheepVsPigMonsterLocations(int TeamNumber) {
		 Location startLocation = new Location(Bukkit.getWorld("Mini_Games"),0,6,0);
		switch (TeamNumber) {
		
		case 1 :
			startLocation.setX(100);
			startLocation.setY(91);
			startLocation.setZ(25);
			break;
		case 2 :
			startLocation.setX(100);
			startLocation.setY(91);
			startLocation.setZ(125);
			break;
		case 3 :
			startLocation.setX(200);
			startLocation.setY(91);
			startLocation.setZ(25);
			break;
		case 4 :
			startLocation.setX(200);
			startLocation.setY(91);
			startLocation.setZ(125);
			break;
		case 5 :
			startLocation.setX(100);
			startLocation.setY(91);
			startLocation.setZ(225);
			break;
		case 6 :
			startLocation.setX(200);
			startLocation.setY(91);
			startLocation.setZ(225);
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
	
	public Location MJStompThemAllLocation(int i) {
		Random r = new Random();
		int rx = r.nextInt(11)-5; 
		if (i%2 == 0) {
			return new Location(Bukkit.getWorld("Mini_Games"),-10+rx,4,18.5);
		} else {
			return new Location(Bukkit.getWorld("Mini_Games"),-10+rx,4,13.5);
		}
	}
	
    public void MJArenePvPInfos() {
    	Bukkit.broadcastMessage("§4MINIJEU : ARENE DE COMBAT EN EQUIPE");
    	Bukkit.broadcastMessage("§eRègles :");
    	Bukkit.broadcastMessage("§e    - Combat à Mort par équipes.");
    	Bukkit.broadcastMessage("§e    - Le stuff est distribué à l'entré (Tank/DPS)");
    	Bukkit.broadcastMessage("§e    - Difficulté du monde : HARD");
    	Bukkit.broadcastMessage("§e    - Mode de jeu : Aventure");
    	Bukkit.broadcastMessage("§e    - Une epee en diamant est cachée dans l'arène");
    	Bukkit.broadcastMessage("§e    - La dernière équipe vivante remporte la victoire");
    }
    
	public void MJSheepVsPigInfos() {
    	Bukkit.broadcastMessage("§4MINIJEU : SHEEPS vs PIGS");
    	Bukkit.broadcastMessage("§eRègles :");
    	Bukkit.broadcastMessage("§e    - Chaque équipe est teleporté sur une petite map. Un poulet apparait au centre.");
    	Bukkit.broadcastMessage("§e    - Tuer un poulet (ou un autre monstre/animal) en fait apparaitre 2 nouvelles chez les autres équipes.");
    	Bukkit.broadcastMessage("§e    - La dernière équipe avoir moins de " + maxEntities + " monstre/animal sur son terrain remporte la victoire !");
    	Bukkit.broadcastMessage("§e    - Tuer une entité vous donne de l'or en fonction de son type (de 2 à 450).");
    	Bukkit.broadcastMessage("§e    - Vous recevez également un [Income] (revenu) d'or toute les 15 secondes.");
    	Bukkit.broadcastMessage("§e    - Un marché vous permet d'utiliser cet or pour :");
    	Bukkit.broadcastMessage("§e    - Acheter des [Items] : armures, armes, consommables.");
    	Bukkit.broadcastMessage("§e    - Acheter des [Spawns] : Fait spawn un certain nombre de monstres chez un ennemi au hasard et augmente votre [Income] pour 1/8 du prix.");
    	Bukkit.broadcastMessage("§e    - Acheter des [Mob LvL] : Le type de l'entitée apparaissant sur votre zone lorsqu'un ennemi en tue un sur son territoire");
    	Bukkit.broadcastMessage("§e depend de la [Somme des Mob Lvls] achetés par tous vos ennemis. Les paliers sont plus difficiles à atteindre lorsque le nombre d'équipes en jeu est grand.");
    	Bukkit.broadcastMessage("§e    - Une fontaine de vie dans un coin de votre map vous rendra 1 coeur toute les 2 secondes si vous vous tenez à moins de 3 cases de celui-ci.");
    	Bukkit.broadcastMessage("§e    - Mode de jeu : Aventure");
    	Bukkit.broadcastMessage("§e    - Difficulté du monde : HARD");
	}
	
	public void MJStompThemAllInfos() {
    	Bukkit.broadcastMessage("§4MINIJEU : STOMP THEM ALL");
    	Bukkit.broadcastMessage("§eRègles :");
    	Bukkit.broadcastMessage("§e    - Utilises les bouttons pour écraser tes ennemies (et tes alliés).");
    	Bukkit.broadcastMessage("§e    - Le dernier joueur en vie remporte la partie !");
    	Bukkit.broadcastMessage("§e    - Mode de jeu : Aventure");
    	Bukkit.broadcastMessage("§e    - Difficulté du monde : HARD");
	}
	
	public static int getNearbyEntitiesNumber(Location l, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		int result = 0;
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
				for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z + (chZ * 16)).getChunk()
						.getEntities()) {
					if (e.getLocation().distance(l) <= radius && e.getLocation().getBlock() != l.getBlock() && e.getType() != EntityType.DROPPED_ITEM && e.getType() != EntityType.ARROW && e.getType() != EntityType.TIPPED_ARROW && e.getType() != EntityType.PLAYER)
						result++;
				}
			}
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

	public void MJReward(Player player) {
		
		Block block = player.getLocation().getBlock();
		//CHEST INVENTORY
		block.setType(Material.CHEST);
		Location chestLocation = block.getLocation();
		double x = chestLocation.getX();
		double y = chestLocation.getY();
		double z = chestLocation.getZ(); 
		player.sendMessage("Votre récompense est apparu à la position : X = " + x + "; Y = " + y + "; Z = " + z);
		Chest chest = (Chest) block.getState();
		Inventory chestInventory = chest.getInventory();
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		chestInventory.addItem(RandomItem());
		
		/*// X+1
		block = L.add(i, 0, 0).getBlock();
		while (block.getType() != Material.AIR) {
			// Z + i
			block = L.add(0, 0, i).getBlock();
			// Z - i
			if (block.getType() != Material.AIR) {
				block = L.add(0, 0, -i).getBlock();
				// X - i
				if (block.getType() != Material.AIR) {
					block = L.add(-i, 0, 0).getBlock();
					// X + i
					if (block.getType() != Material.AIR) {
						block = L.add(i, 0, 0).getBlock();
						// X + i : Z + i
						if (block.getType() != Material.AIR) {
							block = L.add(i, 0, i).getBlock();
							// X + i : Z - i
							if (block.getType() != Material.AIR) {
								block = L.add(i, 0, -i).getBlock();
								// X - i : Z + i
								if (block.getType() != Material.AIR) {
									block = L.add(-i, 0, i).getBlock();
									// X - i : Z - i
									if (block.getType() != Material.AIR) {
										block = L.add(-i, 0, -i).getBlock();
										// SAME Y+1
										// Z + i
										if (block.getType() != Material.AIR) {
											block = L.add(0, 1, i).getBlock();
											// Z - i
											if (block.getType() != Material.AIR) {
												block = L.add(0, 1, -i).getBlock();
												// X - i
												if (block.getType() != Material.AIR) {
													block = L.add(-i, 1, 0).getBlock();
													// X + i
													if (block.getType() != Material.AIR) {
														block = L.add(i, 1, 0).getBlock();
														// X + i : Z + i
														if (block.getType() != Material.AIR) {
															block = L.add(i, 1, i).getBlock();
															// X + i : Z - i
															if (block.getType() != Material.AIR) {
																block = L.add(i, 1, -i).getBlock();
																// X - i : Z + i
																if (block.getType() != Material.AIR) {
																	block = L.add(-i, 1, i).getBlock();
																	// X - i : Z - i
																	if (block.getType() != Material.AIR) {
																		block = L.add(-i, 1, -i).getBlock();
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}

				}
			}
			i++;
		}*/	}
	
	public void deleteWorld() {
        Bukkit.getServer().unloadWorld("Mini_Games", true);
        /*File dir = new File("Mini_Games");
        try {
            FileUtils.deleteDirectory(dir);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }*/
	}

	public void copyWorld(){
		//File dataFolder = new File(Bukkit.getWorldContainer().getPath());
        //String strData = dataFolder.toString();
        //String[] split = strData.toString().split(File.pathSeparator);
        
        //String rootFolder = split[split.length];
        //File root = new File(rootFolder);
     
        File srcDir = new File("Mini_Games_Original");
        if (!srcDir.exists()) {
            Bukkit.getLogger().warning("[Mini_Games_Original] does not exist!");
            return;
        }
        File destDir = new File("Mini_Games");
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Bukkit.getServer().createWorld(new WorldCreator("Mini_Games"));
    }

	public void holograms(Location location, String text) {
		ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND); //Spawn the ArmorStand

		as.setGravity(false); //Make sure it doesn't fall
		as.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
		as.setCustomName(text); //Set this to the text you want
		as.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
		as.setVisible(false); //Makes the ArmorStand invisible

	}

	public void MJSheepVsPigHolograms(MainGame mg) {
		int not = mg.getNumberOfTeams();
		int i = 1;
		for (i = 1; i <= not; i++) {
			holograms(MJSheepVsPigPlayerLocations(i),"§4Fontaine de vie : (+ 0.5 coeur/s");
			holograms(MJSheepVsPigMonsterLocations(i),"§4Spawn des Monstres");
		}
		
	}
}








