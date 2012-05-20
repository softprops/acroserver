package acro

import java.util.UUID

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import org.jboss.netty.util.{TimerTask,Timeout}
import org.jboss.netty.example.http.websocketx.server._

import Handler._

object Timer {
  val underlying = new org.jboss.netty.util.HashedWheelTimer
  def apply[U](delay: Long, unit: TimeUnit)(f: => U) {
    underlying.newTimeout(new TimerTask {
      def run(timeout: Timeout) {
        f
      }
    }, delay, unit)
  }
  def seconds[U](delay: Long) = Timer(delay, TimeUnit.SECONDS)_
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
    case Answer(con) if room.getState == Room.State.WRITING_ACRONYMS =>
      rounds.head.addAnswer(con.request.getUserId,
                            new Acronym(con.request.getUserId,
                                        con.request.optString("acronym")))
  } }

  def broadcast(str: String) {
    println("broadcasting: " + str)
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
    rounds.head.setRound(rounds.size)
    val text = Handler.gsonHeavy.toJson(new Response("sr", rounds.head))
    broadcast(text)
    Timer.seconds(61) {
      val answers = Handler.gsonHeavy.toJson(
        new Response("as", rounds.head.getAnswers))
      broadcast(answers)
      room.startVoting()
    }
  }
}
