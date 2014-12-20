package concurrency

import akka.actor.ActorSystem
import configuration.SiteSettings
import scaldi.Module
import scaldi.akka.AkkaInjectable
import spray.can.server.ServerSettings

package object actor {

  val BlockingDispatcher = "akka.blocking-dispatcher"

  class ActorsModule extends Module {
    bind [ActorSystem] to ActorSystem("system") destroyWith (_.shutdown())
    binding toProvider new HealthCheck()
    binding toProvider new BackGround()

    binding identifiedBy 'backgroundActor toNonLazy  {
      implicit val system = inject [ActorSystem]
      AkkaInjectable.injectActorRef[BackGround]("background-service")
    }
  }

}
