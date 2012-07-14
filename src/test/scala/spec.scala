package acro

import org.specs._
import tubesocks.{ Message => TsMessage, _ }
import java.util.concurrent.{ CountDownLatch, TimeUnit }

object AcroSpec extends Specification
  with unfiltered.spec.netty.Served {

  def setup = _.handler(AcroPlan(new Handler).at("websocket"))

  def uri = Seq(host.to_uri.toString.replace("http", "ws"), "websocket")
                .mkString("")

  "acro" should {
    "join users" in {     
      val l = new CountDownLatch(1)
      Sock.uri(uri) {
        case Open(_) =>
          println("connection open")
        case TsMessage(s, t) =>
          println("got srvr msg %s" format t)
          l.countDown
        case Close(_) =>
          println("connection close")
      }
      l.await(200, TimeUnit.MILLISECONDS)
    }
  }
}
