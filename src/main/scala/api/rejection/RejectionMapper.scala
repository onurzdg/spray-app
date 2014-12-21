package api.rejection

import api.CommonTraits
import api.directive.{InvalidCsfrLoginTokenRejection, InvalidCsfrSessionTokenRejection}
import api.domain.RequestResponse
import api.rejection.CustomRejections.{AccountLockedRejection, PasswordIncorrectRejection, UserDoesNotExistRejection}
import spray.http.StatusCodes
import spray.routing._


private[api]
trait RejectionMapper { self: CommonTraits =>

  val doNotTamperWithCookiesMsg = "Do not tamper with cookies"
  val wrongCredentials = "Credentials are wrong"

  val restrictedAPIsRejectionHandler = RejectionHandler {
    case MissingCookieRejection(cookieName) ::  _ =>
      log.info(s"missing cookie $cookieName")
      deleteCookiesAndRedirect("authenticate properly")

    case  InvalidCookieRejection(cookieName) :: _ =>
      log.info("invalid cookie was sent")
      deleteCookiesAndRedirect(doNotTamperWithCookiesMsg)

    case InvalidCsfrSessionTokenRejection :: _ =>
      log.info("invalid session csrf token was sent")
      deleteCookiesAndRedirect(doNotTamperWithCookiesMsg)

    case InvalidCsfrLoginTokenRejection :: _ =>
      log.info("invalid login csrf token was sent")
      deleteCookiesAndRedirect(doNotTamperWithCookiesMsg)

    case MissingHeaderRejection(missingHeader) :: _ =>
      log.info(s"missing header: $missingHeader")
      deleteCookiesAndRedirect(s"send the header $missingHeader")

    case PasswordIncorrectRejection(email) ::_ =>
      log.info(s"Password is wrong for $email")
      complete(StatusCodes.OK, RequestResponse(false, None: Option[Int], None, List(wrongCredentials)))

    case MalformedRequestContentRejection(msg, _) :: _ =>
      log.info(msg)
      complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List(msg)))

    case Nil => /* secret code for path not found */
      resourceNotFound
  }

  val unrestrictedAPIsRejectionHandler = RejectionHandler {

    case MissingHeaderRejection(missingHeader) :: _ =>
      log.info(s"missing header: $missingHeader")
      complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List(s"send the header $missingHeader")))

    case PasswordIncorrectRejection(email) ::_ =>
      log.info(s"Password is wrong for $email")
      complete(StatusCodes.OK, RequestResponse(false, None: Option[Int], None, List(wrongCredentials)))

    case AccountLockedRejection(email) ::_ =>
      log.info(s"Account is locked for $email")
      complete(StatusCodes.OK, RequestResponse(false, None: Option[Int], None, List("your account is locked")))

    case UserDoesNotExistRejection(email) ::_ =>
      log.info(s"No account with email $email exists")
      complete(StatusCodes.OK, RequestResponse(false, None: Option[Int], None, List(wrongCredentials)))

    case InvalidCsfrLoginTokenRejection :: _ =>
      log.info("invalid login csrf token was sent")
      complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List("do not tamper with token")))

    case MalformedRequestContentRejection(msg, _) :: _ =>
      complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List(msg)))
  }

  val mainRejectionHandler = RejectionHandler {
    case Nil => /* secret code for path not found */
      resourceNotFound

    case _ =>
      complete(StatusCodes.BadRequest, RequestResponse(false, None: Option[Int], None, List("bad request")))
  }
}