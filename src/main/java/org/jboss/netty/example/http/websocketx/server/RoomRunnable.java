package org.jboss.netty.example.http.websocketx.server;

import org.jboss.netty.example.http.websocketx.server.Room.State;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.google.gson.Gson;

public class RoomRunnable implements Runnable {

	Room room;

	boolean canRun = true;

	Gson gson;

	acro.Handler handler;

	public RoomRunnable(Room room, acro.Handler handler) {
		super();
		this.handler = handler;
	}
	
	private int wait_writing_acronyms = 60;
	private int wait_voting = 45;
	private int wait_chat = 10;

	@Override
	public void run() {
		while (canRun) {
			if (room.getState() == State.CHATTING) {
				handleChatState();
			} else if (room.getState() == State.VOTING) {
				handleVoting();
			} else if (room.getState() == State.WRITING_ACRONYMS) {
				handleWritingAcronyms();
			}
		}
	}

	private void handleWritingAcronyms() {
		
	}

	private void handleVoting() {
		// TODO Auto-generated method stub
		
	}

	private void handleChatState() {
		try {
			wait(wait_chat);
		} catch (InterruptedException e) {
		}		
		if(room.hasEnoughPlayers()) {
			startWritingAcronyms();
		}
	}

	private void startWritingAcronyms() {
		// TODO Auto-generated method stub
		room.startRound();
	}

	private void sendMessageToAll(Object obj) {
		for (Player player : room.getPlayers()) {
			player.getContext().getChannel()
					.write(new TextWebSocketFrame(gson.toJson(obj)));
		}
	}

}
