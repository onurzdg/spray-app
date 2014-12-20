package api.directive


import component.account.domain._
import component.account.domain.Account
import api.CommonTraits
import api.rejection.CustomRejections._
import spray.http.RemoteAddress
import spray.routing._

import scala.util.Left

private[api]
trait Authentication { self: CommonTraits =>

  def authenticateUser(signIn: SignIn, ipAddress: Option[RemoteAddress]): Directive1[Account] = {
    onSuccess(accountService.authenticateAccount(signIn, ipAddress)).
      flatMap {
      case Left(failure) => failure match {
        case WrongPassword(email) => reject(PasswordIncorrectRejection(email))
        case AccountLocked(email) => reject(AccountLockedRejection(email))
        case UserDoesNotExist => reject(UserDoesNotExistRejection(signIn.email))
      }
      case Right(acc) => provide(acc)
    }
  }
}
