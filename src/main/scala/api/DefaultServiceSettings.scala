package api

import akka.util.Timeout
import spray.routing.HttpService

import scala.concurrent.duration._

private[api]
trait DefaultServiceSettings { self: HttpService =>
  implicit val executionContext = actorRefFactory.dispatcher
  implicit val askTimeout: Timeout = 3.seconds
}
