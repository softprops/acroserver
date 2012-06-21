package org.jboss.netty.example.http.websocketx.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
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
		CHATTING,VOTING,WRITING_ACRONYMS,FACE_OFF
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
	
	public List<Player> getLeaders() {
		List<Player> leaders = new ArrayList<Player>(players.values());
		Collections.sort(leaders, new Comparator<Player>() {
		
			public int compare(Player a, Player b) {
				int c = Integer.valueOf(a.getTotalVoteCount()).compareTo(b.getTotalVoteCount());
				if(c!=0) {
					return c;
				}
				if(rounds.isEmpty()) {
					return c;
				}
				Round r = rounds.get(rounds.size()-1);
				Acronym aa = r.getAnswer(a.getUserId());
				Acronym bb = r.getAnswer(b.getUserId());
				if(aa==null) {
					return 1;
				}
				if(bb==null) {
					return -1;
				}
				return Long.valueOf(aa.getReceived()).compareTo(bb.getReceived());
			}
			
		});
		return leaders;
	}

	public int getRoomSize() {
		return players.size();
	}
	
	public boolean hasEnoughPlayers() {
		return players.size() > 2;
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

	public void removePlayer(String userId) {
		players.remove(userId);
		playerCount = players.size();
	}

    public Player getPlayer(String userId) {
        return players.get(userId);
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

	public void startVoting() {
		state = State.VOTING;
	}
	
	public void startChatting() {
		state = State.CHATTING;
	}
	
	public void startFaceOff() {
		state = State.FACE_OFF;
	}
	
}
