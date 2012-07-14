package acro

import scala.util.control.Exception.allCatch
import unfiltered.request.{ Path, Seg }
import unfiltered.netty.Http
import unfiltered.netty.websockets.{ Message => WsMessage, _ }

case class AcroPlan(hand: Handler) {
  def at(P: String) = Planify {
    case Path(Seg(P :: Nil)) => {
      case WsMessage(s, Text(txt)) =>
        hand.handleRequest(s.channel, txt)
    }
  }
}

object Main {
  val DefaultPort = 8080
  def main(a: Array[String]) {
    Http(a match {
      case Array(p) =>
        allCatch.opt(Integer.parseInt(p))
                .getOrElse(DefaultPort)
      case _ => DefaultPort
    }).handler(AcroPlan(new Handler)
               .at("websocket")).run
  }
}
