package api.directive

import shapeless.{::, HNil}
import spray.http.RemoteAddress
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.MiscDirectives.clientIP
import spray.routing.{Directive, Directive0}

private[api]
trait CustomMiscDirectives {

  def conditional(check: => Boolean,directive0: Directive0) : Directive0 =
    if (check) directive0
    else pass
    //else new Directive0 { def happly(f: HNil => Route) = f(HNil)}

  lazy val optionalClientIP: Directive[Option[RemoteAddress] :: HNil] =
    clientIP.map(Option[RemoteAddress]).recoverPF {case Nil => provide(None)}
}

object CustomMiscDirectives extends CustomMiscDirectives
