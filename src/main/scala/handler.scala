package acro

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame
import com.google.gson.{ExclusionStrategy,FieldAttributes,FieldNamingPolicy,
                        Gson,GsonBuilder}

import scala.collection.JavaConverters._

import org.jboss.netty.example.http.websocketx.server._

case class Context(channelContext: ChannelHandlerContext,
                   request: Request) {
  def write(str: String) {
    Handler.write(channelContext, str)
  }
}

class Handler {
  import Handler._
  def handleRequest(ctx: ChannelHandlerContext, msg: String) {
    val request = new Request(msg)
    val con = Context(ctx, request)
    try {
      if(request.isMessage()) {
        game ! Message(con)
      } else {
        if (request.isAutoJoin()) {
          game ! AutoJoin(con)
        } else if (request.isRoomListRequest()) {
          game ! RoomList(con)
        } else if (request.isJoinRoomRequest()) {
          game ! Join(con)
        }
      }
    } catch {
      case e =>
        e.printStackTrace()
        val response = new Response("er", null);
        response.setStatus("error");
        con.write(gsonLight.toJson(response))
    }
  }

  val game = new Game
  game.start()

}
object Handler {
  def write(channelContext: ChannelHandlerContext, str: String) {
    channelContext.getChannel().write(new TextWebSocketFrame(str))
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
