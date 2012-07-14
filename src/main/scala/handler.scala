package acro

import org.jboss.netty.channel.Channel
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import com.google.gson.{ExclusionStrategy,FieldAttributes,FieldNamingPolicy,
                        Gson,GsonBuilder}

import scala.collection.JavaConverters._

import org.jboss.netty.example.http.websocketx.server.Request

trait Action {
  def con: Context
}
trait RoomAction extends Action
case class RoomList(con: Context) extends Action
case class AutoJoin(con: Context) extends Action
case class Join(con: Context) extends Action
case class Message(con: Context) extends RoomAction
case class Answer(con: Context) extends RoomAction
case class Vote(con: Context) extends RoomAction
case class Leave(con: Context) extends RoomAction

case class Context(channel: Channel,
                   request: Request) {
  def write(str: String) {
    try {
      Handler.write(channel, str)
    } catch {
      case e => e.printStackTrace
    }
  }
  val actions = Map[String, (Context => Action)](
    "rl" -> RoomList,
    "jr" -> Join,
    "m"  -> Message,
    "aj" -> AutoJoin,
    "aa" -> Answer,
    "vt" -> Vote,
    "lv" -> Leave
  )
  def getAction = actions(request.getType)(this)
}

class Handler {
  import Handler._
  def handleRequest(ctx: Channel, msg: String) {
    println(msg)
    game ! Context(ctx, new Request(msg)).getAction
  }
  val game = new Game
  game.start()

}
object Handler {
  def write(chan: Channel, str: String) {
    if (chan.isConnected) {
      chan.write(new TextWebSocketFrame(str))
    }
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
          classOf[Channel].isAssignableFrom(arg)

        override def shouldSkipField(arg0: FieldAttributes) =
          classOf[Channel].isAssignableFrom(
            arg0.getDeclaredClass)
      }).create
}
