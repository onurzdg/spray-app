package api.actor

import akka.actor.Actor
import api.Routes
import scaldi.{Injectable, Injector}



class SprayServiceActor(implicit val inj: Injector) extends Actor with Injectable with Routes {
  log.info("Starting service actor and http server.")
  implicit override def actorRefFactory = context

  def receive = runRoute {
    routes
  }
}
