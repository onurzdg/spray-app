package concurrency.actor

import java.util

import akka.actor.{ActorLogging, Actor}
import com.codahale.metrics.health.HealthCheckRegistry
import scaldi.{Injector, Injectable}

import scala.language.postfixOps
import scala.concurrent.duration._

import com.codahale.metrics.health.HealthCheck.Result


object HealthCheck {
  private case object Check
  case object Stop
  case object Start

  case object Status
  case class Status(res: util.SortedMap[String, Result])
}


private[actor]
class HealthCheck(implicit inj: Injector) extends Actor with ActorLogging with Injectable {
  import HealthCheck._
  import context.dispatcher
  private val healthCheckRegistry = inject[HealthCheckRegistry]
  var scheduler = starChecking()

  def starChecking() = {
    val scheduler = context.system.scheduler.schedule(initialDelay = 10 seconds, interval = 30 seconds) {
      self ! Check
    }
    scheduler
  }

  def receive: Receive = {
    case Start => starChecking()
    case Check =>  healthCheckRegistry.runHealthChecks()
    case Status => sender ! Status(healthCheckRegistry.runHealthChecks())
    case Stop => scheduler.cancel()
  }
}
