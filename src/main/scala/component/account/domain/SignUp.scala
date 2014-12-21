package component.account.domain

import component.account.domain.Password.ClearTextPassword

import scala.language.implicitConversions


case class SignUp(name:Name, email:Email, password:ClearTextPassword)

object SignUp extends SignUpJsonProtocol with SignUpImplicitsLow

trait SignUpJsonProtocol extends PasswordJsonProtocol {
  implicit val ajaxSignUpFormat = jsonFormat3(SignUp.apply)
}

trait SignUpImplicitsLow {
  implicit def signUpToAccount(signUp:SignUp): Account =  Account(name=signUp.name,email=signUp.email,password=signUp.password)
}