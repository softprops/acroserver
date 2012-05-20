package org.jboss.netty.example.http.websocketx.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.google.gson.annotations.Expose;

public class Room implements Serializable {

	@Expose
	private String id;

	@Expose
	private boolean isAdult;

	@Expose
	private String name;
	
	@Expose
	private int playerCount;
	
	private Map<String,Player> players = new HashMap<String,Player>();
	
	private List<Round> rounds = new ArrayList<Round>(5);
	
	private Round currentRound;
	
	private State state = State.CHATTING;
	
	public State getState() {
		return state;
	}
	
	public enum State {
		CHATTING,VOTING,WRITING_ACRONYMS
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public int getPlayerCount() {
		return playerCount;
	}
	
	public Collection<Player> getPlayers() {
		return players.values();
	};
	
	public int getRoomSize() {
		return players.size();
	}
	
	public Player getWinnerOfLastRound() {
		Player player = null;
		if(!rounds.isEmpty()) {
			Round round = rounds.get(rounds.size()-1);
			Acronym acro = round.getWinner();
			String winnerUserId = acro.getUserId();
			player = players.get(winnerUserId);
		} else {
			Iterator<Player> playersIterator = players.values().iterator();
			Player max = playersIterator.next();
				
			for(Player playa : players.values()) {
				if(playa.lastResponseReceived>max.lastResponseReceived) {
					max = playa;
				}
			}
			player = max;
		}
		return player;
	}
	
	public boolean hasEnoughPlayers() {
		return players.size() > 1;
	}
	
	public boolean isAdult() {
		return isAdult;
	}
	
	public boolean isFull() {
		return players.size() > 13;
	}
	
	public void join(ChannelHandlerContext ctx, Request request) {
		String userId = request.getUserId();
		Player player = null;
		if(userId==null) {
			userId = UUID.randomUUID().toString();			
		}
		player = players.get(userId);
		if(player==null) {
			player = new Player(ctx);
			player.setUserId(userId);
			player.setUsername(request.getUsername());
			player.setAvatarUrl(request.getAvatarUrl());
		}
		players.put(userId, player);
		playerCount = players.size();
	}
	
	public void setAdult(boolean isAdult) {
		this.isAdult = isAdult;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	public void startRound() {
		state = State.WRITING_ACRONYMS;
	}
	
	private Round getRound() {
		if(currentRound==null) {
			Round r = new Round();
			int size = rounds.size()+3;
			int acroSize;
			if(size%7==0) {
				acroSize = 7;
			} else if(size%6==0) {
				acroSize = 6;
			} else if(size%5==0) {
				acroSize = 5;
			} else if(size%4==0) {
				acroSize = 4;
			} else {
				acroSize = 3;
			}
			
			StringBuilder b = new StringBuilder(acroSize);
			for(int i = 0; i < acroSize; i++) {
				b.append("A");
			}
			r.setAcronym(b.toString());
			r.setCategory("baseball");
			currentRound = r;
		}
		return currentRound;
	}
	
}
