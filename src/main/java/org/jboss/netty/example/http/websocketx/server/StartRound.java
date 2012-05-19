package org.jboss.netty.example.http.websocketx.server;

import java.util.List;

public class StartRound {
	
	private int round;
	
	private List<Player> players;
	
	private String acronym;
	
	public StartRound(int round, List<Player> players, String acronym) {
		this.round = round;
		this.players = players;
		this.acronym = acronym;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	
	

}
