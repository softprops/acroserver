package acro

import scala.collection.JavaConverters._
import scala.actors.Actor

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.jboss.netty.example.http.websocketx.server._

import Handler._

class Game extends Actor {
  private var rooms = Map.empty[String, RoomActor]
  def roomsData = rooms.values.map { _.room }.asJava

  def act = loop { react {
    case RoomList(con) =>
      println("RoomList("+con.request+")")
      if (rooms.isEmpty) {
        Seq("The Lounge", "Cloud Nine", "Sin City").foreach(newRoom)
      }
      con.write(gsonLight.toJson(new Response("rl", roomsData)))
    case Join(con) =>
      println("Join("+con.request+")")
      rooms(con.request.getRoom()) ! Join(con)
    case AutoJoin(con) =>
      println("AutoJoin("+con.request+")")
      val available = rooms.values.filter { !_.room.isFull }
      val room =
        if (available.isEmpty)
          newRoom("Auto Room")
        else
          available.minBy { _.room.getRoomSize }
      room ! Join(con)
    case Message(con) =>
      println("Message("+con.request+")")
      val room = rooms(con.request.getRoom())
      con.request.remove("type")
      con.request.remove("room")
      for(player <- room.players) {
        player.getContext.getChannel.write(
          new TextWebSocketFrame(gsonHeavy.toJson(
            new Response("m", con.request.getMessage()))))
      }
    case ans: RoomAction =>
      for (rm <- room(ans.con)) rm ! ans
  }}
  def room(con: Context) = rooms.get(con.request.getRoom)
  def newRoom(name: String) = {
    val roomActor = new RoomActor(name)
    rooms = rooms + (roomActor.room.getId() -> roomActor)
    roomActor.start()
    roomActor
  }
}
