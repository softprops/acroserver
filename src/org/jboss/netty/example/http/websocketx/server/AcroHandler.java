package org.jboss.netty.example.http.websocketx.server;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AcroHandler {

	private Gson gsonHeavy;
	private Gson gsonLight;

	public AcroHandler() {
		GsonBuilder builder = new GsonBuilder().setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.excludeFieldsWithoutExposeAnnotation();
		gsonLight = builder.create();
		builder = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		builder.setExclusionStrategies(new ExclusionStrategy() {

			@Override
			public boolean shouldSkipClass(Class<?> arg) {
				if(ChannelHandlerContext.class.isAssignableFrom(arg)) {
					return true;
				}
				return false;
			}

			@Override
			public boolean shouldSkipField(FieldAttributes arg0) {
				if(ChannelHandlerContext.class.isAssignableFrom(arg0.getDeclaredClass())) {
					return true;
				}
				return false;
				
			}
			
		});
		gsonHeavy = builder.create();
	}

	private Comparator<Room> LOWEST_FIRST = new Comparator<Room>() {
		@Override
		public int compare(Room o1, Room o2) {
			return Integer.valueOf(o1.getRoomSize())
					.compareTo(o2.getRoomSize());
		}
	};

	private Map<String, Room> roomIdToRoom = new HashMap<String, Room>();
	private Set<Room> rooms = new HashSet<Room>();
	
	public Set<Room> getRooms() {
		return rooms;
	}

	public void handleRequest(ChannelHandlerContext ctx, String msg)
			throws Exception {
		Request request = new Request(msg);
		Object response = null;
		String type = null;
		boolean light = true;
		try {
			if(request.isMessage()) {
				handleMessage(ctx, request);
				return;
			}
			if (request.isAutoJoin()) {
				type = "jr";
				light = false;
				response = handleAutoJoin(ctx, request);
			} else if (request.isRoomListRequest()) {
				type = "rl";
				light = true;
				response = handleRoomList(request);
			} else if (request.isJoinRoomRequest()) {
				type = "jr";
				light = false;
				response = handleJoin(ctx, request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (response != null) {
			Gson gson = null;
			if (light) {
				gson = gsonLight;
			} else {
				gson = gsonHeavy;
			}
			try {
				String str = gson.toJson(new Response(type, response));
				ctx.getChannel().write(
						new TextWebSocketFrame(str));
			}catch (Exception e) {
				throw e;
			}
		} else {
			response = new Response(type,null);
			((Response)response).setStatus("error");
			ctx.getChannel().write(new TextWebSocketFrame(gsonLight.toJson(response)));
		}
	}

	private void handleMessage(ChannelHandlerContext ctx, Request request) {
		System.out.println("handleMessage("+request+")");
		Room room = roomIdToRoom.get(request.getRoom());
		request.remove("type");
		request.remove("room");
		for(Player player : room.getPlayers()) {
			player.getContext().getChannel().write(new TextWebSocketFrame(gsonHeavy.toJson(new Response("m",request.getMessage()))));
		}
	}

	private Object handleJoin(ChannelHandlerContext ctx, Request request) {
		System.out.println("handleJoin("+request+")");
		Room room = roomIdToRoom.get(request.getRoom());
		if (!room.isFull()) {
			room.join(ctx, request);
		}
		return room;
	}

	private Collection<Room> handleRoomList(Request request) {
		System.out.println("handleRoomList("+request+")");
		if (rooms.isEmpty()) {
			Room room = newRoom(request);
			rooms.add(room);
			roomIdToRoom.put(room.getId(), room);;
		}
		return rooms;
	}

	private Object handleAutoJoin(ChannelHandlerContext ctx, Request request) {
		Room room = null;
		if (rooms.isEmpty()) {
			room = newRoom(request);
		} else {
			for (Room r : rooms) {
				if (r.isFull()) {
					continue;
				}
				if (r.getRoomSize() > 5 && r.getRoomSize() < 10) {
					room = r;
					break;
				} else {
					if (room != null) {
						if (r.getRoomSize() > 5
								&& r.getRoomSize() > room.getRoomSize()) {
							room = r;
							break;
						}
					} else {
						room = r;
					}
				}
			}
		}
		room.join(ctx, request);
		return room;
	}

	private Room newRoom(Request request) {
		Room room = new Room();
		room.setName("Ryan's Room");
		room.setAdult(false);
		room.setId(UUID.randomUUID().toString());
		return room;
	}

}
