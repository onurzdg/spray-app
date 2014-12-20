package api.directive

import component.security.crypto.CryptoService
import api.CommonTraits
import spray.routing._

/**
 * Cross-site forgery directive
 */

case object InvalidCsfrSessionTokenRejection extends Rejection
case object InvalidCsfrLoginTokenRejection extends Rejection

private[api]
trait Csfr { self: CommonTraits =>
  private val cryptoService = inject[CryptoService]

  def generateLoginCsrfToken(data: String) =  cryptoService.encryptAndSignData(data)

  def generateCsrfToken(data: String): Directive1[String] = provide(cryptoService.encryptAndSignTimeSensitiveData(data))

  def verifyCsrfToken: Directive0 =
    headerValueByName(siteSettings.sessionCsrfToken).flatMap { headerVal =>
      cryptoService.verifySignatureAndDecryptTimeSensitiveData(headerVal) match {
        case Some(token) => pass
        case None => reject(InvalidCsfrSessionTokenRejection)
      }
    }

  def verifyLoginCsrfToken: Directive0 =
    headerValueByName(siteSettings.loginCsfrToken).flatMap{ headerVal =>
      cryptoService.verifySignatureAndDecryptData(headerVal) match {
        case Some(token) => pass
        case None =>  reject(InvalidCsfrLoginTokenRejection)
      }
    }

  def checkActionMethodSessionToken: Directive0 =
    extract(_.request.method.name.toUpperCase).flatMap({
      case "POST" | "PUT" | "DELETE"  =>  verifyCsrfToken
      case _ => pass

    })


}