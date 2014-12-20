package component.account.domain

import component.account.domain.Password.ClearTextPassword

case class PasswordUpdate(password:ClearTextPassword, newPassword: ClearTextPassword)

object PasswordUpdate extends PasswordUpdateJsonProtocol

trait PasswordUpdateJsonProtocol extends PasswordJsonProtocol {
  implicit val ajaxUpdateFormat = jsonFormat2(PasswordUpdate.apply)
}
