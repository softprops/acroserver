package org.jboss.netty.example.http.websocketx.server;

import com.google.gson.annotations.Expose;

public class Response {

	@Expose
	String type;
	
	@Expose
	String status = "success";
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Response(String type, Object data) {
		super();
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Expose
	Object data;
	
}
