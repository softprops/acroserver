package org.jboss.netty.example.http.websocketx.server;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;

public class Acronym {

	long received;

	@Expose
	String text;

	@Expose
	Player player;
	
	@Expose
	int voteCount;
	
	public Acronym(Player player, String text) {
		this.player = player;
		this.text = text;
	}
	
	public Acronym addVote(String voterId) {
		votes.add(voterId);
		voteCount = votes.size();
		return this;
	}
	
	public Acronym removeVote(String voterId) {
		votes.remove(voterId);
		voteCount = votes.size();
		return this;
	}

	public int getVoteCount() {
		return voteCount;
	}

	Set<String> votes = new HashSet<String>();

	public long getReceived() {
		return received;
	}

	public String getText() {
		return text;
	}

	public Player getPlayer() {
		return player;
	}

	public void setReceived(long received) {
		this.received = received;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
