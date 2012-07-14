package acro

import org.specs._
import tubesocks.{ Message => TsMessage, _ }
import java.util.concurrent.{ CountDownLatch, TimeUnit }

object AcroSpec extends Specification
  with unfiltered.spec.netty.Served {

  def setup = _.handler(AcroPlan(new Handler).at("websocket"))

  def uri = Seq(host.to_uri.toString.replace("http", "ws"), "websocket")
                .mkString("")

  def awaiting[T](action: => (()  => Unit) => Unit)(then: => T) = {
    val l = new CountDownLatch(1)
    action({ l.countDown })
    l.await
    then
  }

  "acro" should {
    "list rooms" in {
      awaiting({ complete =>
        Sock.uri(uri) {
          case Open(s) =>
            s.send("""{"type":"%s"}""" format Cmd.roomList)
          case TsMessage(s, t) =>
            println("got room list resp %s" format t)
            complete
        }
      }){
        // make assertion here
      }
    }
    "join users" in {     
      awaiting({ complete =>
        Sock.uri(uri) {
          case Open(s) =>
            s.send("""{"type":"%s"}""" format Cmd.join)
          case TsMessage(s, t) =>
            println("got join resp %s" format t)
            complete
        }
      }){
        // make assertion here
      }
    }
  }
}
