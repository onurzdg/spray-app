package component.account.domain

import component.account.domain.Password.ClearTextPassword


case class SignIn(email:Email, password: ClearTextPassword, rememberMe: Boolean)

object SignIn extends SignInJsonProtocol

trait SignInJsonProtocol extends PasswordJsonProtocol {
  implicit val ajaxSignInFormat = jsonFormat3(SignIn.apply)
}