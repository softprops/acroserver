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
case class Disconnected(userId: String)

class RoomActor(name: String) extends scala.actors.Actor { self =>
  def answerTime = 20
  def voteTime = 20

  val room = new Room()
  room.setName(name)
  room.setAdult(false)
  room.setId(UUID.randomUUID().toString())

  def players = room.getPlayers.asScala

  def cleanup: Unit = {
    for (player <- players)
      if (!player.getContext.getChannel.isConnected)
        self ! Disconnected(player.getUserId)
    Timer.seconds(1)(cleanup)
  }
  Timer.seconds(5)(cleanup)

  var rounds = List.empty[Round]

  def act = loop { react {
    case Join(con) =>
      if (!room.isFull()) {
        room.join(con.channelContext, con.request)
      }
      broadcast(gsonHeavy.toJson(new Response("jr", room)))
      if (room.getState == Room.State.CHATTING) {
        startRound()
      }
    case Message(con) =>
      con.request.remove("type")
      con.request.remove("room")
      broadcast(gsonHeavy.toJson(
        new Response("m", con.request.getMessage())))
    case Answer(con) if room.getState == Room.State.WRITING_ACRONYMS =>
      rounds.head.addAnswer(con.request.getUserId,
                            new Acronym(room.getPlayer(con.request.getUserId),
                                        con.request.optString("acronym")))
      val answerCount = Handler.gsonHeavy.toJson(
        new Response("ac", rounds.head.getAcronyms.size))
      broadcast(answerCount)
    case Vote(con) =>
      rounds.head.addVote(con.request.getUserId,
                          con.request.optString("acronym"))
    case Leave(con) =>
      room.removePlayer(con.request.getUserId)

    case Disconnected(userId) =>
      println("removing " + userId)
      room.removePlayer(userId)
  } }

  def broadcast(str: String) {
    println("broadcasting: " + str)
    for (player <- room.getPlayers.asScala) {
      Handler.write(player.getContext, str)
    }
  }
  val rand = new scala.util.Random
  def startRound() {
    if (!room.hasEnoughPlayers) {
      room.startChatting()
    } else {
      room.startRound()
      val size = (rounds.size % 2) + 3
      val chars = "ABCDEFGHIJKLMNOPQRSTVW".toSeq
      val acro = rand.shuffle(chars).take(size).mkString
      rounds = new Round :: rounds
      rounds.head.setCategory("general")
      rounds.head.setAcronym(acro)
      rounds.head.setRound(rounds.size)
      val text = Handler.gsonHeavy.toJson(new Response("sr", rounds.head))
      broadcast(text)
      Timer.seconds(answerTime + 5) {
        if (rounds.head.getAnswers.isEmpty) {
          startRound()
        } else {
          val answers = Handler.gsonHeavy.toJson(
            new Response("as", rounds.head.getAnswers))
          broadcast(answers)
          room.startVoting()
          Timer.seconds(voteTime + 1) {
            val answers = Handler.gsonHeavy.toJson(
              new Response("vc", rounds.head.getAnswers))
            broadcast(answers)
            for {
              player <- players
              answer <- Option(rounds.head.getAnswer(player.getUserId))
            } {
              player.setTotalVoteCount(
                player.getTotalVoteCount + answer.getVoteCount
              )
            }
            Timer.seconds(10) {
              startRound()
            }
          }
        }
      }
    }
  }
}

