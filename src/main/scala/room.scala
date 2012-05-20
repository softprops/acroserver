package acro

import java.util.UUID

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import org.jboss.netty.example.http.websocketx.server._

import Handler._

object Timer {
  val underlying = new org.jboss.netty.util.HashedWheelTimer
}

class RoomActor extends scala.actors.Actor {
  val room = new Room()
  room.setName("Ryan's Room")
  room.setAdult(false)
  room.setId(UUID.randomUUID().toString())

  def players = room.getPlayers.asScala

  var rounds = List.empty[Round]

  def act = loop { react {
    case Join(con) =>
      if (!room.isFull()) {
        room.join(con.channelContext, con.request)
      }
      con.write(gsonHeavy.toJson(new Response("jr", room)))
      if (room.getState == Room.State.CHATTING && room.hasEnoughPlayers) {
        startRound()
      }
  } }

  def broadcast(str: String) {
    for (player <- room.getPlayers.asScala) {
      Handler.write(player.getContext, str)
    }
  }
  val rand = new scala.util.Random
  def startRound() {
    room.startRound()
    val size = (rounds.size % 4) + 3
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWY".toSeq
    val acro = (for (_ <- (1 to size))
      yield chars(rand.nextInt(chars.size))).mkString
    rounds = new Round :: rounds
    rounds.head.setCategory("general")
    rounds.head.setAcronym(acro)
    val sr = new StartRound(rounds.size, room.getPlayers, acro)
    broadcast(Handler.gsonHeavy.toJson(sr))
  }
}
