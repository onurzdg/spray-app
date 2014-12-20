package api.route

import api.CommonTraits
import api.directive.Privilege._
import akka.pattern.ask
import scala.concurrent.duration._
import spray.can.Http
import spray.can.server.Stats
import spray.http.ContentTypes
import spray.httpx.marshalling.Marshaller
import spray.util._

private[api]
trait SprayStat {self: CommonTraits =>

  def sprayStatRoute = {
    path("stats") {
      sessionCookie { session =>
        hasAccess(session("id").toLong, Super) {
          complete {
            actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")
              .ask(Http.GetStats)(1.second)
              .mapTo[Stats]
          }
        }
      }
    }
  }

  implicit val statsMarshaller: Marshaller[Stats] =
    Marshaller.delegate[Stats, String](ContentTypes.`text/plain`) { stats =>
        "Uptime                : " + stats.uptime.formatHMS + '\n' +
        "Total requests        : " + stats.totalRequests + '\n' +
        "Open requests         : " + stats.openRequests + '\n' +
        "Max open requests     : " + stats.maxOpenRequests + '\n' +
        "Total connections     : " + stats.totalConnections + '\n' +
        "Open connections      : " + stats.openConnections + '\n' +
        "Max open connections  : " + stats.maxOpenConnections + '\n' +
        "Requests timed out    : " + stats.requestTimeouts + '\n'
    }

}