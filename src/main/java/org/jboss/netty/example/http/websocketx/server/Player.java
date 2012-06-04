package org.jboss.netty.example.http.websocketx.server;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.json.JSONObject;
import com.google.gson.annotations.Expose;

public class Player extends JSONObject {
	
	private ChannelHandlerContext ctx;
	
	public Player(ChannelHandlerContext ctx) {
		super();
		this.ctx = ctx;
	}
	
	@Expose
	String userId;
	
	@Expose
	String username;

	@Expose
	int totalVoteCount;
	
	String avatarUrl;
	
	public ChannelHandlerContext getContext() {
		return ctx;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public int getTotalVoteCount() {
		return totalVoteCount;
	}
	public void setTotalVoteCount(int totalVoteCount) {
		this.totalVoteCount = totalVoteCount;
	}
	
	
}
