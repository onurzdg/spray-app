package component.account.dao

import java.sql.Timestamp
import java.time.{ZoneOffset, LocalDateTime}

import component.account.domain.Password.{ClearTextPassword, EncryptedPassword}
import component.account.domain.{Password, Name, Email}
import spray.http.{DateTime, RemoteAddress}
import storage.postgres.dbSimple._

object TypeMappers {
  trait HttpIpTypeMapper {
    implicit val httpIpTypeMapper = MappedColumnType.base[RemoteAddress,String]( _.value, RemoteAddress(_))
  }

  trait LocalDateTimeTypeMapper {
    implicit val dateTimeTypeMapper = MappedColumnType.base[LocalDateTime, Timestamp](
    { case (dt: LocalDateTime) => new Timestamp(dt.toInstant(ZoneOffset.UTC).getEpochSecond * 1000 ) },
    { case (dt: Timestamp) => dt.toLocalDateTime })
  }

  trait EmailTypeMapper {
    implicit val emailTypeMapper = MappedColumnType.base[Email, String](a => a.email, new Email(_))
  }

  trait NameTypeMapper {
    implicit val nameTypeMapper = MappedColumnType.base[Name, String](a => a.name, new Name(_))
  }

  trait PasswordTypeMapper {
    // only allow encrypted passwords to be persisted
    implicit val passwordTypeMapper = MappedColumnType.base[Password, String](
    { case EncryptedPassword(pwHash) => pwHash
      case ClearTextPassword(_) => throw new IllegalArgumentException("only encrypted password can be persisted ")
    },
    { case pwHash => EncryptedPassword(pwHash) })
  }
}



