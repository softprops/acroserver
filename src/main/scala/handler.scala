package acro

import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame

import scala.collection.JavaConverters._

import org.jboss.netty.example.http.websocketx.server._

case class Context(channelContext: ChannelHandlerContext,
                   request: Request) {
  def write(str: String) {
    channelContext.getChannel().write(new TextWebSocketFrame(str))
  }
}

class Handler { handler =>

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
        con.write(game.gsonLight.toJson(response))
    }
  }

  val game = new Game
  game.start()

}
