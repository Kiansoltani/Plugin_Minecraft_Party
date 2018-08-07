package fr.zarten.pluginminecraftparty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.entity.Player;

public class Equipe {
	Player[] membres;
	String name;
	
	
	public Equipe(Player[] player, String name) {
		this.membres = player;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Player[] getmembres() {
		return membres;
	}

	public void setmembres(Player[] membres) {
		this.membres = membres;
	}
	
	public void saveTeam() {
		File file = new File("teams");
		try {
			FileWriter fw = new FileWriter (file, true);
			fw.write (this.name);
			for (Player p : this.membres) {
				fw.write(":");
				fw.write (p.getName());
			}
			fw.write("\r\n");
			fw.close();
		} catch (IOException e) {
			System.out.println ("Erreur lors de la lecture/ecriture du fichier 'team.txt' ");
			e.printStackTrace();
		}
	}
	
	public Player getTeamLeader() {
		return this.membres[0];
	}
	
	public boolean IsPlayerInTeam(Player p) {
		String Name = p.getName();
		int i;
		boolean result = false;
		for (i = 0; i < this.getmembres().length; i++) {
			if (this.getmembres()[i].getName().equalsIgnoreCase(Name)) {
				result = true;
			}
		}
		return result;
	}
}
