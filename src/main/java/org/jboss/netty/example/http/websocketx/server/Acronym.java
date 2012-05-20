package org.jboss.netty.example.http.websocketx.server;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;

public class Acronym {

	long received;

	@Expose
	String text;

	@Expose
	String userId;
	
	@Expose
	int voteCount;
	
	public Acronym(String userId, String text) {
		this.userId = userId;
		this.text = text;
	}
	
	public Acronym voteFor(Player player) {
		votes.add(player);
		voteCount = votes.size();		
		return this;
	}
	
	public Acronym devoteFor(Player player) {
		votes.remove(player);
		voteCount = votes.size();
		return this;
	}

	Set<Player> votes = new HashSet<Player>();

	public long getReceived() {
		return received;
	}

	public String getText() {
		return text;
	}

	public String getUserId() {
		return userId;
	}

	public void setReceived(long received) {
		this.received = received;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
