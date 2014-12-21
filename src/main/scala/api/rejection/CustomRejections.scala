package api.rejection

import component.account.domain.Email
import spray.routing.Rejection

private[api]
trait CustomRejections {
  case object MissingSessionCookieRejection extends Rejection
  case object MissingRememberMeCookieRejection extends Rejection

  trait AuthenticationRejection extends Rejection
  case class PasswordIncorrectRejection(email: Email) extends AuthenticationRejection
  case class AccountLockedRejection(email: Email) extends AuthenticationRejection
  case class UserDoesNotExistRejection(email: Email) extends AuthenticationRejection

}

private[api]
object CustomRejections extends CustomRejections