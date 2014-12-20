package api.route

import html.{notFound, page}
import api._
import api.domain.RequestResponse
import spray.http.StatusCodes
import spray.routing.RejectionHandler._
import spray.routing._

private[api]
trait Misc { self: CommonTraits =>

  import api.url.static._

  def invalidCookieRoute(cookieName: String): Route = {
    requestUri { uri ⇒
      log.info(s"Invalid cookie $cookieName: user is probably messing with cookie")
      if(uri.path.toString().startsWith(s"/$ApiRoot/")) {
        complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List("bad cookie")))
      }
      else {
        deleteCookies() {
          redirect(signIn, StatusCodes.Found) // No authenticated cookies - clean up and redirect to sign in
        }
      }
    }
  }

  def resourceNotFound: Route = {
      requestUri{ uri =>
        log.info(s"resource not found: $uri")
        if(uri.path.toString().startsWith(s"/$ApiRoot/")) {
          complete(StatusCodes.NotFound)
        }
        else {
          complete(StatusCodes.NotFound, page(notFound()))
        }
      }
  }

  def deleteCookiesAndRedirect(error: String): Route = {
    requestUri { uri ⇒
      if(uri.path.toString().startsWith(s"/$ApiRoot/")) {
        deleteCookies() {
          complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], Some(signIn), List(error)))
        }

      }
      else {
        deleteCookies() {
          redirect(signIn, StatusCodes.Found)
        }
      }
    }
  }

  val invalidCookiePF: PF = {
    case  InvalidCookieRejection(cookieName) :: _ =>
      invalidCookieRoute(cookieName)
  }


}
