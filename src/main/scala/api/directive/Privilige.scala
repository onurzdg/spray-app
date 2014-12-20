package api.directive

import api.CommonTraits
import spray.routing._


private[api]
object Privilege extends Enumeration {
  val Regular = Value(0)
  val Super = Value(1)
  val Admin = Value(2)
}

private[api]
trait Privilege { self: CommonTraits =>
  def hasAccess(accountId: Long, minRequiredPriv: Privilege.Value)(route: Route): Route = {
    onSuccess(accountService.retrieveAccount(accountId)) {    // replace this part with your own implementation
      case Some(acc) => route
      case _ => reject(AuthorizationFailedRejection)
    }
  }
}