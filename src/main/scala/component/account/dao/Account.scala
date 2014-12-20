package component.account.dao

import java.time.LocalDateTime
import java.util.UUID

import component.account.dao.TypeMappers._
import component.account.domain._
import concurrency.ExecutionContexts
import configuration.SiteSettings
import grizzled.slf4j.Logger
import metrics.MetricsInstrumented
import spray.http.RemoteAddress
import storage.postgres.dbSimple._
import scala.slick.jdbc.JdbcBackend.Session

import scala.slick.lifted.TableQuery

trait Mappers extends NameTypeMapper
with EmailTypeMapper with PasswordTypeMapper with HttpIpTypeMapper

private[account]
class AccountTable(tag: Tag) extends Table[Account](tag, "account") with Mappers {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[Name]("name", O.NotNull)
  def email = column[Email]("email", O.NotNull)

  def password = column[Password]("password", O.NotNull)

  def activatedAt = column[LocalDateTime]("activated_at", O.Nullable)
  def suspendedAt = column[LocalDateTime]("suspended_at", O.Nullable)

  def loginCount = column[Int]("login_count", O.NotNull)
  def failedLoginCount = column[Int]("failed_login_count", O.NotNull)
  def lockedOutUntil = column[LocalDateTime]("locked_out_until", O.Nullable)
  def currentLoginAt = column[LocalDateTime]("current_login_at", O.Nullable)
  def lastLoginAt = column[LocalDateTime]("last_login_at", O.Nullable)
  def currentLoginIp = column[RemoteAddress]("current_login_ip", O.Nullable)
  def lastLoginIp = column[RemoteAddress]("last_login_ip", O.Nullable)

  def createdAt = column[LocalDateTime]("created_at", O.NotNull)
  def updatedAt = column[LocalDateTime]("updated_at", O.Nullable)

  def resetToken = column[String]("reset_token", O.Nullable)
  def resetRequestedAt = column[LocalDateTime]("reset_requested_at", O.Nullable)

  def * =
    (id.?, name, email, password,
      activatedAt.?, suspendedAt.?,
      loginCount, failedLoginCount, lockedOutUntil.?,
      currentLoginAt.?, lastLoginAt.?, currentLoginIp.?, lastLoginIp.?,
      createdAt.?, updatedAt.?, resetToken.?, resetRequestedAt.?) <>
      ((Account.apply _).tupled, Account.unapply)
}


private[account]
class Accounts extends TableQuery(new AccountTable(_)) with MetricsInstrumented with Mappers {
  implicit val dbExecContext = ExecutionContexts.dbExecutionContext
  import storage.operationSuccessMapper
  import scala.slick.jdbc.JdbcBackend.Session

  private[this] val logger = Logger[this.type]
  private val siteSettings = SiteSettings()

  val accounts = TableQuery[AccountTable]

  private val qRetrieveAccountByEmail = Compiled( (email: Column[Email]) =>
    for { account <- accounts if account.email === email } yield account)

  private val qRetrieveAccountPassword = Compiled( (id: Column[Long]) =>
    for { account <- accounts if account.id === id } yield account.password )

  private val qRetrieveAccount = Compiled( (id: Column[Long]) =>
    for { account <- accounts if account.id === id } yield account ) // or accounts.filter(_.id === account.id.get)

  private val qRetrieveAccountName = Compiled( (id: Column[Long]) =>
    for { account <- accounts if account.id === id } yield account.name )

  private val qRetrieveAccountEmail = Compiled((id: Column[Long]) =>
    for { account <- accounts if account.id === id } yield account.email )

  def createAccount(account: Account)(implicit session: Session): Account = {
    logger.info("account created")
    val pw = Password.encrypt(siteSettings.encryptionLogRounds)(account.password)
    // set random values
    val createdAt = account.createdAt.getOrElse(LocalDateTime.now)
    val currentLoginAt, lastLoginAt = LocalDateTime.now
    val acc: Account = account.copy(password = pw, createdAt = Some(createdAt))
    val id = accounts.returning(accounts.map(_.id)) += acc
    account.copy(id = Some(id))
  }


  def retrieveAccount(id: Long)(implicit session: Session): Option[Account] = {
    logger.info("account retrieved1")
    qRetrieveAccount(id).firstOption
  }

  def retrieveAccountPassword(id: Long)(implicit session: Session): Option[Password] =
    qRetrieveAccountPassword(id).firstOption


  def retrieveAccountByEmail(email: Email)(implicit session: Session): Option[Account] =
    qRetrieveAccountByEmail(email).firstOption


  def updateAccount(account: Account)(implicit session: Session): Boolean =
    operationSuccessMapper(qRetrieveAccount(account.id.get).update(account))

  def updateAccountPassword(id: Long, password: Password)(implicit session: Session): Boolean =
    operationSuccessMapper(qRetrieveAccountPassword(id).update(Password.encrypt(siteSettings.encryptionLogRounds)(password)))


  def updateAccountEmail(id: Long, email: Email)(implicit session: Session) =
    operationSuccessMapper(qRetrieveAccountEmail(id).update(email))


  def updateAccountName(id: Long, name: Name)(implicit session: Session): Boolean =
    operationSuccessMapper(qRetrieveAccountName(id).update(name))


  def deleteAccount(id: Long)(implicit session: Session): Boolean =
    operationSuccessMapper(qRetrieveAccount(id).delete)
}




