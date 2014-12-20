package concurrency.actor

import akka.actor.{Actor, ActorLogging}
import scaldi.Injector
import scaldi.akka.AkkaInjectable



object BackGround {
    case object GetHealthCheckStatus
    case object PauseHealthCheck
}

class BackGround(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {
  import concurrency.actor.BackGround._
  private val healthCheckActorProps = injectActorProps[HealthCheck]
  healthCheckActorProps.withDispatcher(BlockingDispatcher)
  private val healthCheckActorRef = context.actorOf(healthCheckActorProps, "health-check-actor")

  override def receive: Receive = {
    case GetHealthCheckStatus => healthCheckActorRef ! HealthCheck.Status
    case HealthCheck.Status(status) =>
    case HealthCheck.Stop => healthCheckActorRef ! HealthCheck.Stop
  }
}









