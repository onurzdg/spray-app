package component.account.domain


sealed trait AuthenticationFailure

case class WrongPassword(email: Email) extends AuthenticationFailure
case class AccountLocked(email: Email) extends AuthenticationFailure
case object UserDoesNotExist extends AuthenticationFailure