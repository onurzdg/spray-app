package api.directive

import component.security.crypto.CryptoService
import api.CommonTraits
import api.domain.SessionCookie
import spray.http.{DateTime, HttpCookie}
import spray.routing._
import shapeless._
import scala.language.implicitConversions

private[api]
trait SessionCookieDirectives { self: CommonTraits =>
  private val cryptoService = inject[CryptoService]

  case class InvalidCookieRejection(cookieName: String) extends Rejection

  def serializeData(data: Map[String, String]): String =
    data.filterNot(_._1.contains(":")).map(d => d._1 + ":" + d._2).mkString("\u0000")

  def deSerialize(message: String) =
    message.split("\u0000").map(_.split(":")).map(p => p(0) -> p(1)).toMap

  def sessionCookie: Directive1[SessionCookie] = {
    validateSessionCookie(siteSettings.sessionCookieName)
  }

  def rememberMeCookie: Directive1[SessionCookie] = {
    validateSessionCookie(siteSettings.rememberCookieName)
  }

  private def validateSessionCookie(cookieName: String): Directive1[SessionCookie] = {
    val sessionCookie = cookie(cookieName).map(sessionCookieFromCookie)
    sessionCookie.flatMap({
      case Right(cookie) => provide(cookie)
      case Left(rejection) =>  reject(rejection)  // update failure to login with a rememberme cookie
    })
  }

  def optionalRememberMe: Directive[Option[SessionCookie] :: HNil] =
    rememberMeCookie.hmap(_.map(shapeless.option)) | provide(None)

  def optionalCookies: Directive1[Option[HttpCookie]] = {
    (optionalCookie(siteSettings.sessionCookieName) & optionalCookie(siteSettings.rememberCookieName)).hmap {
      case sessCook :: rememberMeCook :: HNil =>
        sessCook.orElse(rememberMeCook)
    }
  }

  def optionalSession: Directive[Option[SessionCookie] :: HNil] =
    sessionCookie.hmap(_.map(shapeless.option)) | provide(None)

  def setSession(session: SessionCookie): Directive0 =
    setCookie(sessionCookieToCookie(siteSettings.sessionCookieName,
      session.copy(expires = Some( DateTime.now + (siteSettings.sessionCookieMaxAge * 1000)),
        maxAge = Some(siteSettings.sessionCookieMaxAge))))

  def setRememberMe(session: SessionCookie): Directive0 = setCookie(sessionCookieToCookie(siteSettings.rememberCookieName,
    session.copy(expires = Some( DateTime.now + (siteSettings.rememberCookieMaxAge * 1000)),
      maxAge = Some(siteSettings.rememberCookieMaxAge))))

  def deleteSessionCookie(domain: String = "", path: String = ""): Directive0 = {
    deleteCookie(siteSettings.sessionCookieName, domain, path)
  }

  def deleteRememberMeCookie(domain: String = "", path: String = ""): Directive0 = {
    deleteCookie(siteSettings.rememberCookieName, domain, path)
  }

  def deleteCookies(): Directive0 = {
    val root = api.url.static.root
    deleteSessionCookie(path = root) &
      deleteRememberMeCookie(path = root)
  }

  implicit def sessionCookieFromCookie(cookie: HttpCookie): Either[Rejection, SessionCookie] =
    cryptoService.verifySignatureAndDecryptTimeSensitiveData(cookie.content) match {
      case Some(cookieContent) =>
        Right(SessionCookie(deSerialize(cookieContent), cookie.expires, cookie.maxAge, cookie.domain, cookie.path,
          cookie.secure, cookie.httpOnly, cookie.extension))
      case _ =>
        Left(InvalidCookieRejection(siteSettings.sessionCookieName))
    }

  private def sessionCookieToCookie(cookieName: String, session: SessionCookie): HttpCookie =
    HttpCookie(cookieName, cryptoService.encryptAndSignTimeSensitiveData(serializeData(session.data)),
      session.expires, session.maxAge, session.domain,
      session.path, session.secure, session.httpOnly, session.extension)
}