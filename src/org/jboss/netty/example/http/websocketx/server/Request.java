package org.jboss.netty.example.http.websocketx.server;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Request extends JSONObject {

	public Request(String str) throws Exception {
		super(str);
	}
	
	public boolean isAutoJoin() {
		return "aj".equals(optString("type"));
	}
	
	public boolean isRoomListRequest() {
		return "rl".equals(optString("type"));
	}
	
	public String getUserId() {
		return optString("user_id");
	}
	
	public String getUsername() {
		return optString("username");
	}
	
	public String getRoom() {
		return optString("room");
	}
	
	public boolean isMessage() {
		return "m".equals(optString("type"));
	}

	public boolean isJoinRoomRequest() {
		return "jr".equals(optString("type"));
	}
	
	public Map<String,Object> getMessage() {
		Map<String,Object> obj = new HashMap<String,Object>();
		obj.put("username", optString("username"));
		obj.put("message", optString("message"));
		obj.put("user_id", optString("user_id"));
		return obj;
	};

	@Override
	public String toString() {
		return "Request [isAutoJoin()=" + isAutoJoin()
				+ ", isRoomListRequest()=" + isRoomListRequest()
				+ ", getUserId()=" + getUserId() + ", getUsername()="
				+ getUsername() + ", getRoom()=" + getRoom()
				+ ", isJoinRoomRequest()=" + isJoinRoomRequest()
				+ ", toString()=" + super.toString() + "]";
	}
	
	public String toJson() {
		return super.toString();
	}
	
	
	
}
