
import akka.actor.ActorSystem
import component.chat.actor.Chat
import api.actor.SprayServiceActor
import scaldi.Module
import scaldi.akka.AkkaInjectable
import spray.can.server.ServerSettings

package object api {

  object url {
    object static {
      private[api] val root = "/"
      private[api] val signIn = "/signin"
      private[api] val signUp = "/signUp"
    }
  }
  val ApiRoot = "api"

  object scheme {
    private[api] val https = "https"
    private[api] val http = "http"
  }

  private[api] val resourceDir = "theme"

  class HttpModule extends Module {
    binding to ServerSettings(inject [ActorSystem])
    binding toProvider new SprayServiceActor()
    binding toProvider new Chat()

    binding identifiedBy 'sprayActor to {
      implicit  val system = inject [ActorSystem]
      AkkaInjectable.injectActorRef[SprayServiceActor]("spray-service")
    }
  }
}
