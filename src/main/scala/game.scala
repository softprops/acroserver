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

  val mainScreen = new org.jboss.netty.channel.group.DefaultChannelGroup 

  def act = loop { react {
    case RoomList(con) =>
      println("RoomList("+con.request+")")
      if (rooms.isEmpty) {
        Seq("The Lounge", "Cloud Nine", "Sin City").foreach(newRoom)
      }
      con.write(gsonLight.toJson(new Response("rl", roomsData)))
      mainScreen.add(con.channelContext.getChannel)
    case Join(con) =>
      println("Join("+con.request+")")
      rooms(con.request.getRoom()) ! Join(con)
      mainScreen.remove(con.channelContext.getChannel)
      mainScreen.write(new TextWebSocketFrame(
        gsonLight.toJson(new Response("rl", roomsData))
      ))
    case AutoJoin(con) =>
      println("AutoJoin("+con.request+")")
      val available = rooms.values.filter { !_.room.isFull }
      val room =
        if (available.isEmpty)
          newRoom("Auto Room")
        else
          available.minBy { _.room.getRoomSize }
      room ! Join(con)
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
