package fr.zarten.pluginminecraftparty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MainGame {
	Equipe[] gameteams;

	public MainGame() {
		try {
			gameteams = this.loadAllTeam();
		} catch (IOException e) {
			System.out.println("Fichier Team illisible ou introuvable");
			e.printStackTrace();
		}
	}

	public Equipe[] getGameteams() {
		return gameteams;
	}

	public void setGameteams(Equipe[] gameteams) {
		this.gameteams = gameteams;
	}
	public  Equipe getTeamByPlayer(Player player) {
		Player[] ps;
		Equipe result = null;
		for (Equipe t : gameteams) {
			ps = t.getmembres();
			for (Player p : ps) {
				if (player == p) {
					result = t;
				}
			}
		}
		return result;
	}
	public  Equipe getTeamByPlayerName(String playerName) {
		Player[] ps;
		Equipe result = null;
		for (Equipe t : gameteams) {
			ps = t.getmembres();
			for (Player p : ps) {
				if (Bukkit.getServer().getPlayer(playerName) == p) {
					result = t;
				}
			}
		}
		return result;
	}
	
	public int getPlayerNumber() {
		int result = 0;
		Player[] ps;
		for (Equipe t : gameteams) {
			ps = t.getmembres();
			for (@SuppressWarnings("unused") Player p : ps) {
					result++;
				}
			}
		return result;
		}
	
	public Equipe[] loadAllTeam() throws IOException {

		//OPENING FILE
			File f = new File("teams");
			FileReader fr = new FileReader(f);
			FileReader fr2 = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			BufferedReader br2 = new BufferedReader(fr2);
			String[] params;
			String line;
			
			//COMPTE DU NOMBRE D'EQUIPES
			int nbt = 0;
			line = br2.readLine();
			while (line != null) {
				nbt++;
				line = br2.readLine();
			}
			br2.close();
			fr2.close();
			
			//CREATION DES TEAMS
			
			line = br.readLine();
			int j = 0;
			int i;
			Equipe[] teams = new Equipe[nbt];
			while (line != null) {

				params = line.split(":");
				Player[] p = new Player[params.length-1];
				for (i = 1; i < params.length ; i++) {
					p[i-1] = Bukkit.getPlayer(params[i]);
				}
				
				teams[j] = new Equipe(p,params[0]);
				line = br.readLine();
				j++;
			}
			br.close();
			fr.close();
		return teams;
	}


	public String getTeamLeaderByTeamNumber(int teamNumber) {
		String result;
		Player p[] = gameteams[teamNumber].getmembres();
		result = p[0].getName();
		return result;
	}
	
	
	//retourne 0 si le joueur n'est dans aucune equipe
	public int getTeamNumberByTeamPlayer(Player p) {
		int i;
		int result = 0;
		for (i = 0; i < gameteams.length; i++) {
			if (gameteams[i].IsPlayerInTeam(p)) {
				result = i+1;
			}
		}
		
		return result;
	}
	
	public int getNumberOfTeams() {
		return gameteams.length;
	}
	
	public Equipe getTeambyTeamNumber(int teamNumber) {
		return gameteams[teamNumber-1];
	}
	
	public Player getPlayerbyName(String p) {
		Player player = null;
		for (Equipe e : gameteams) {
			for (Player pl : e.getmembres()) {
				if (pl.getName().equalsIgnoreCase(p)) {
					player = pl;
				}
			}
		}
		return player;
	}
}



















