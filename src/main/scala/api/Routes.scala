package api

import api.domain.SessionCookie
import api.rejection.RejectionMapper
import api.route._
import spray.http.StatusCodes._
import spray.httpx.encoding.{Deflate, Gzip, NoEncoding}
import spray.routing.MissingCookieRejection

private[api]
trait ResourceRoutes extends Account with Chat
  with Picture with SprayStat with Public with Root { self: CommonTraits =>
}

trait Routes extends CommonTraits with ExceptionMapper with HttpLogger with RejectionMapper with ResourceRoutes {

  def restrictedRoutes(implicit session: SessionCookie) = {
    rootRoute ~ accountRoute ~ pictureRoute ~ chatRoute
  }
  
  def unRestrictedAPIs() = {
    publicRoutes ~ accountSignUpRoute
  }

  def routes =
    decompressCompress {
      logRequestResponse(logErrorResponses _) {
        routeHttpToHttps ~ handleRejections(mainRejectionHandler) {
          getFromResourceDirectory(api.resourceDir) ~
            scheme(api.scheme.https) {
              handleRejections(unrestrictedAPIsRejectionHandler) {
                unRestrictedAPIs()
              }
            } ~ scheme(api.scheme.https) {
            handleRejections(restrictedAPIsRejectionHandler) {
              optionalCookies {
                case Some(_) =>
                  checkActionMethodSessionToken {
                    restrictedAPIs
                  }
                case None => requestUri { uri ⇒ // user does not have any cookies, thus cannot be logged in
                  // user is trying to access main page or a restricted API
                  val mainPageOrRestrictedApi = uri.path.toString() == "/" || uri.path.toString().startsWith(s"/$ApiRoot/")
                  if(mainPageOrRestrictedApi) {
                    reject(MissingCookieRejection(siteSettings.sessionCookieName))
                  }
                  else { // user is trying to access something that does not exist(404)
                    reject
                  }
                }
              }
            }
          }
        }
      }
    }

  def decompressCompress = decompressRequest() & compressResponse(Gzip, Deflate, NoEncoding)

  val restrictedAPIs = {
    sessionCookie { implicit session =>
      restrictedRoutes
    } ~ optionalCookie(siteSettings.rememberCookieName) {
      case Some(cookie) =>
        rememberMeCookie { session =>
          implicit val sessionCookie =
            SessionCookie(data = Map("id" -> session("id")), path = Some("/"))
          (setSession(sessionCookie) & setRememberMe(sessionCookie)) {
            restrictedRoutes
          }
        }
      case None => reject
    }
  }


  val routeHttpToHttps = scheme(api.scheme.http) {
    extract(_.request.uri) { uri ⇒
      redirect(uri.copy(scheme = api.scheme.https), MovedPermanently)
    }
  }
}
