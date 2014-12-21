package component.account.domain

import component.account.domain.Password.{ClearTextPassword, EncryptedPassword}
import org.mindrot.jbcrypt.BCrypt
import spray.json.DefaultJsonProtocol


sealed trait Password {
  import Password._

  override def equals(that:Any) = that match {
    case that: Password => verify(this,that)
    case  _ => false
  }
}

object Password {
  case class ClearTextPassword(pw: String) extends Password {
    require(Option(pw).exists(_.length >= 6), "Password should contain at least six characters")
  }
  case class EncryptedPassword(pw: String) extends Password

  def verify(password1:Password, password2:Password): Boolean = (password1,password2) match {
    case (ClearTextPassword(pw1), ClearTextPassword(pw2)) => pw1 == pw2
    case (EncryptedPassword(pw1), EncryptedPassword(pw2)) => pw1 == pw2
    case (ClearTextPassword(pw1), EncryptedPassword(pw2)) => verifyPassword(pw1,pw2)
    case (EncryptedPassword(pw2), ClearTextPassword(pw1)) => verifyPassword(pw1,pw2)
  }

  def encrypt(logRounds:Int): (Password) => Password = {
    case pw@EncryptedPassword(_) => pw
    case ClearTextPassword(pw) => EncryptedPassword(makePassword(pw,logRounds))
  }

  private def makePassword(pw:String,logRounds:Int) : String =
    BCrypt.hashpw(pw, BCrypt.gensalt(logRounds))

  private def verifyPassword(plaintext:String, hashed:String) : Boolean =
    BCrypt.checkpw(plaintext, hashed)
}

trait PasswordJsonProtocol extends DefaultJsonProtocol {
  import spray.json._

  implicit object passwordJsonFormat extends RootJsonFormat[ClearTextPassword] {
    // only allow encrypted passwords to be serialized
    def write(password: ClearTextPassword): JsValue =
      serializationError("Password: Cannot serialize a clear text password to JSON.")

    def read(value: JsValue) = value match {
      case JsString(password) => ClearTextPassword(password)
      case _ => deserializationError("Password: Excepted a string.")
    }
  }
}