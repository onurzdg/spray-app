package component.account.domain

import java.time.LocalDateTime
import component.account.domain.Password.ClearTextPassword
import spray.http.RemoteAddress
import spray.json._



case class Account(id: Option[Long] = None, name: Name, email: Email                                         // identity
                   ,password: Password                                                                      // password (stored encrypted)
                   ,activatedAt: Option[LocalDateTime] = None, suspendedAt: Option[LocalDateTime] = None              // account activation / suspension
                   ,loginCount: Int = 0, failedLoginCount: Int = 0, lockedOutUntil: Option[LocalDateTime] = None // login failure
                   ,currentLoginAt: Option[LocalDateTime] = None, lastLoginAt: Option[LocalDateTime] = None           // login time
                   ,currentLoginIp: Option[RemoteAddress] = None, lastLoginIp: Option[RemoteAddress] = None // login IP address
                   ,createdAt: Option[LocalDateTime] = None, updatedAt: Option[LocalDateTime] = None                  // record change
                   ,resetToken: Option[String] = None, resetRequestedAt: Option[LocalDateTime] = None            // password reset
                    )

case class AccountCreationFailure(email: Email)

object Account extends AccountJsonProtocol
trait AccountJsonProtocol extends DefaultJsonProtocol {
  implicit object accountJsonFormat extends RootJsonFormat[Account] {

    def write(account: Account): JsValue = account.id match {
      case Some(id) =>
        JsObject(
          "id" -> JsNumber(account.id.get),
          "name" ->  JsString(account.name.name),
          "email" -> JsString(account.email.email)
        )
      case None => serializationError("Account: cannot serialize an uninitialized account.")
    }

    def read(value: JsValue) = value.asJsObject.getFields("id", "name", "email") match {
      case Seq(JsNumber(id), JsString(name), JsString(email)) =>
         Account(id = Some(id.toLong), name= Name(name), email = Email(email), password = null)
      case _ => deserializationError("Account: failed to deserialize")
    }
  }
}


case class Name(name: String) {
  require(Option(name).exists(!_.isEmpty), "name is required")
}

object Name extends DefaultJsonProtocol {
  //implicit val nameFormat = jsonFormat1(Name.apply)
  implicit object nameFormat extends RootJsonFormat[Name] {
    def read(value:JsValue) = value match  {
      case JsString(name) => Name(name)
      case _ =>  deserializationError("Name expected.")
    }

    def write(name:Name) = JsString(name.name)
  }
}

case class Email(email: String) {
  require(Option(email).exists(!_.isEmpty), "email address is required")
}

object Email extends DefaultJsonProtocol {
  implicit object emailFormat extends RootJsonFormat[Email] {
    def read(value:JsValue) = value match  {
      case JsString(email) => Email(email)
      case _ => deserializationError("Email expected.")
    }

    def write(email: Email) = JsString(email.email)
  }

}
