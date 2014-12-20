package concurrency.actor.db

import akka.actor.{ActorLogging, Actor}
import akka.util.Timeout
import scala.concurrent.duration._

trait DbActor extends Actor with ActorLogging  {
  implicit val timeout: Timeout = 2.seconds
}


