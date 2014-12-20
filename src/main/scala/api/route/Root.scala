package api.route

import api.domain.SessionCookie
import api.CommonTraits
import spray.httpx.PlayTwirlSupport
import html._

private[api]
trait Root extends {self: CommonTraits with Misc with PlayTwirlSupport =>


  def rootRoute(implicit session: SessionCookie) = {
    get {
      path("") {
        val userId = session("id")
          generateCsrfToken(userId) { token =>
            complete {
              page(app(token, userId))
            }
          }
      }
    }
  }
}
