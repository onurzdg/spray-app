package concurrency.actor

import akka.actor.{Actor, ActorLogging}
import akka.pattern.CircuitBreaker

import scala.concurrent.duration._
import scala.language.postfixOps


private [actor]
trait DefaultCircuitBreaker {self: Actor with ActorLogging =>
  val resetTimeOut = 1
  import context.dispatcher
  private def notifyOnOpen() {
    this.getClass.getName
    log.warning(s"${this.getClass.getName}: CircuitBreaker is now open, and will not close for $resetTimeOut minute")
  }


  /*  Using circuit breakers allows you to provide fast failure semantics to services and clients.
   *  For example, if they were sending a RESTful request to your system, they wouldn’t have
   *  to wait for their request to time out to know that they’ve failed, since the circuit breaker
   *  will report failure immediately.
   */
  val breaker = new CircuitBreaker(context.system.scheduler,
    maxFailures = 5,
    callTimeout = 4 seconds,
    resetTimeout = resetTimeOut minute).onOpen(notifyOnOpen())
}
