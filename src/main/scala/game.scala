package acro

import java.util.UUID

import scala.collection.JavaConverters._
import scala.actors.Actor

import com.google.gson.{ExclusionStrategy,FieldAttributes,FieldNamingPolicy,
                        Gson,GsonBuilder}
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.jboss.netty.example.http.websocketx.server._

case class RoomList(con: Context)
case class AutoJoin(con: Context)
case class Join(con: Context)
case class Message(con: Context)

class Game extends Actor {
  private var rooms = Map.empty[String, Room]

  def act = loop { react {
    case RoomList(con) =>
      println("RoomList("+con.request+")")
      if (rooms.isEmpty) {
        val room = newRoom(con.request)
        rooms = rooms + (room.getId() -> room)
      }
      con.write(gsonLight.toJson(new Response("rl", rooms.values.asJava)))
    case Join(con) =>
      println("Join("+con.request+")")
      val room = rooms(con.request.getRoom())
      if (!room.isFull()) {
        room.join(con.channelContext, con.request)
      }
      con.write(gsonHeavy.toJson(new Response("jr", room)))
    case AutoJoin(con) =>
      println("AutoJoin("+con.request+")")
      val available = rooms.values.filter { !_.isFull }
      val room =
        if (available.isEmpty)
          newRoom(con.request)
        else
          available.minBy { _.getRoomSize }
      room.join(con.channelContext, con.request)
      con.write(gsonHeavy.toJson(new Response("jr", room)))
    case Message(con) =>
      println("Message("+con.request+")")
      val room = rooms(con.request.getRoom())
      con.request.remove("type")
      con.request.remove("room")
      for(player <- room.getPlayers().asScala) {
        player.getContext.getChannel.write(
          new TextWebSocketFrame(gsonHeavy.toJson(
            new Response("m", con.request.getMessage()))))
      }
    case _ => println("no match")
  }}
  def newRoom(request: Request) = {
    val room = new Room()
    room.setName("Ryan's Room")
    room.setAdult(false)
    room.setId(UUID.randomUUID().toString())
    room
  }
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

}
