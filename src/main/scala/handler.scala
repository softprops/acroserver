package acro

import java.util.{Collection,Comparator,HashMap,HashSet,Map,Set,UUID}

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame

import com.google.gson.{ExclusionStrategy,FieldAttributes,FieldNamingPolicy,
                        Gson,GsonBuilder}
import org.jboss.netty.example.http.websocketx.server._

import scala.collection.JavaConverters._

class Handler {

  val gsonLight =
    new GsonBuilder().setFieldNamingPolicy(
      FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .excludeFieldsWithoutExposeAnnotation().create

  val gsonHeavy =
    new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setExclusionStrategies(new ExclusionStrategy() {
        override def shouldSkipClass(arg: Class[_]) =
          classOf[ChannelHandlerContext].isAssignableFrom(arg)

        override def shouldSkipField(arg0: FieldAttributes) =
          classOf[ChannelHandlerContext].isAssignableFrom(
            arg0.getDeclaredClass)
      }).create

  val LOWEST_FIRST = new Comparator[Room] {
    override def compare(o1: Room, o2: Room) =
      java.lang.Integer.valueOf(o1.getRoomSize).compareTo(o2.getRoomSize())
  }

  private val roomIdToRoom = new HashMap[String, Room]
  private val rooms = new HashSet[Room]
  
  def getRooms() = rooms

  def handleRequest(ctx: ChannelHandlerContext, msg: String) {
    val request = new Request(msg)
    def write(str: String) {
      ctx.getChannel().write(
        new TextWebSocketFrame(str))
    }
    try {
      if(request.isMessage()) {
        handleMessage(ctx, request)
      } else {
        if (request.isAutoJoin()) {
          write(gsonHeavy.toJson(handleAutoJoin(ctx, request)))
        } else if (request.isRoomListRequest()) {
          write(gsonLight.toJson(handleRoomList(request)))
        } else if (request.isJoinRoomRequest()) {
          write(gsonHeavy.toJson(handleJoin(ctx, request)))
        }
      }
    } catch {
      case e =>
        e.printStackTrace()
        val response = new Response("er", null);
        response.setStatus("error");
        write(gsonLight.toJson(response))
    }
  }

  def handleMessage(ctx: ChannelHandlerContext, request: Request) {
    println("handleMessage("+request+")")
    val room = roomIdToRoom.get(request.getRoom())
    request.remove("type")
    request.remove("room")
    for(player <- room.getPlayers().asScala) {
      player.getContext.getChannel.write(
        new TextWebSocketFrame(gsonHeavy.toJson(
          new Response("m",request.getMessage()))))
    }
  }

  def handleJoin(ctx: ChannelHandlerContext, request: Request) = {
    println("handleJoin("+request+")")
    val room = roomIdToRoom.get(request.getRoom())
    if (!room.isFull()) {
      room.join(ctx, request)
    }
    new Response("jr", room)
  }

  def handleRoomList(request: Request) = {
    println("handleRoomList("+request+")")
    if (rooms.isEmpty()) {
      val room = newRoom(request)
      rooms.add(room)
      roomIdToRoom.put(room.getId(), room)
    }
    new Response("rl", rooms)
  }

  def handleAutoJoin(ctx: ChannelHandlerContext, request: Request) = {
    val available = rooms.asScala.filter { !_.isFull }
    val room =
      if (available.isEmpty)
        newRoom(request)
      else
        available.minBy { _.getRoomSize }

    room.join(ctx, request)
    new Response("jr", room)
  }

  def newRoom(request: Request) = {
    val room = new Room()
    room.setName("Ryan's Room")
    room.setAdult(false)
    room.setId(UUID.randomUUID().toString())
    room
  }
}
