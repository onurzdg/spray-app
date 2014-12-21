package api

import configuration.SiteSettings
import api.directive._
import api.domain.CommonJsonProtocols
import api.route.Misc
import metrics.MetricsInstrumented
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import spray.httpx.{PlayTwirlSupport, SprayJsonSupport}
import spray.routing.{Directives, HttpService}
import spray.util.LoggingContext

trait CommonTraits extends HttpService with Directives with Injectable with AkkaInjectable with SprayJsonSupport
  with CommonJsonProtocols with CommonServices with Authentication with Csfr with Privilege with Misc with PlayTwirlSupport
  with DefaultServiceSettings with CustomMiscDirectives with SessionCookieDirectives with MetricsInstrumented {
  implicit val inj: Injector
  val log = LoggingContext.fromActorRefFactory
  val siteSettings = SiteSettings()
}


