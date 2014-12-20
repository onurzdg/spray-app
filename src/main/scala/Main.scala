import akka.actor.{ActorSystem, _}
import akka.io.IO
import com.typesafe.config.{Config, ConfigFactory}
import configuration.SiteSettings
import module._
import api.SSLSupported
import scaldi.Injectable
import scaldi.akka.AkkaInjectable
import spray.can.Http
import spray.can.server.ServerSettings


object Main extends App with SSLSupported with AkkaInjectable with Injectable {

  import Bindings.appInjector

  implicit val system = inject [ActorSystem]
  val log = system.log
  log.info(s"Starting Actor system '${system.name}'.")

  val service = inject [ActorRef] ('sprayActor)

  val siteSettings = inject[SiteSettings]
  val serverSettings = inject[ServerSettings]
  val config: Config = ConfigFactory.load().atKey("thread-pools")

  IO(Http) ! Http.Bind(service, siteSettings.interface, port = siteSettings.port)

  val secondConfig = Some(serverSettings.copy(sslEncryption = false))
  // the only reason we start an additional web server is to redirect http requests to https
  IO(Http) ! Http.Bind(service, siteSettings.interface, port = 8080, settings = secondConfig)

}

