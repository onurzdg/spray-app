package component.account

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import component.account.dao.{Pictures, Accounts}
import component.account.domain._
import concurrency.ExecutionContexts
import configuration.SiteSettings
import grizzled.slf4j.Logger
import metrics.MetricsInstrumented
import nl.grons.metrics.scala.MetricName
import spray.http.RemoteAddress

import scala.concurrent.Future
import scala.slick.jdbc.JdbcBackend._

class AccountService(private val db: Database,
                     private val accountRepo: Accounts,
                     private val pictureRepo: Pictures,
                     private val siteSettings: SiteSettings) extends MetricsInstrumented {
  implicit val dbExecutor = ExecutionContexts.dbExecutionContext
  override lazy val metricBaseName = MetricName("AccountService")

  private[this] val logger = Logger[this.type]
  private[this] val opTimer = metrics.timer("accountOpTimer")

  def createAccount(account: Account): Future[Either[AccountCreationFailure, Account]] =
    Future {
      db withTransaction { implicit session =>
        opTimer.time {
          // check that email address does not already exist
          accountRepo.retrieveAccountByEmail(account.email) match {
            case Some(acc) => Left(AccountCreationFailure(account.email)) // reject account exists
            case _ =>
              val createdAcc = accountRepo.createAccount(account)
              pictureRepo.createPicture(Picture(url = "http://bit.ly/1y3A9HS", accountId = createdAcc.id.get))
              Right(createdAcc)
          }
        }
      }
    }

  def retrieveAccount(accountId: Long): Future[Option[Account]] =
    Future {
      db withSession { implicit session =>
        opTimer.time {
          logger.info("account retrieved")
          accountRepo.retrieveAccount(accountId)
        }
      }
    }


  def retrieveAccountByEmail(email: Email): Future[Option[Account]] =
    Future {
      db withSession { implicit session =>
        accountRepo.retrieveAccountByEmail(email)
      }
    }

  def updateAccount(account: Account): Future[Boolean] =
    Future {
      db withSession { implicit session =>
        accountRepo.updateAccount(account)
      }
    }

  def updateAccountName(accountId: Long, newName: Name): Future[Boolean] =
    Future {
      db withSession { implicit session =>
        accountRepo.updateAccountName(accountId, newName)
      }
    }

  def updateAccountEmail(accountId: Long, newEmail: Email): Future[Boolean] =
    Future {
      db withSession { implicit session =>
        accountRepo.updateAccountEmail(accountId, newEmail)
      }
    }

  def updateAccountPassword(accountId: Long, newPassword: Password): Future[Boolean] =
    Future {
      db withSession { implicit session =>
        accountRepo.updateAccountPassword(accountId, newPassword)
      }
    }

  def deleteAccount(accountId: Long): Future[Boolean] = {
    println("accountId: " + accountId)
    Future {
      db withTransaction  { implicit session =>
        pictureRepo.deletePicturesByAccountId(accountId)
        accountRepo.deleteAccount(accountId)
      }
    }
  }

  def createPicture(picture: Picture): Future[Picture] =
    Future {
      db withSession { implicit session =>
        pictureRepo.createPicture(picture)
      }
    }

  def retrievePictureById(id: Long): Future[Option[Picture]] = {
    Future {
      db withSession { implicit session =>
        pictureRepo.retrievePictureById(id)
      }
    }
  }

  def retrievePictureByAccountId(accountId: Long): Future[List[String]] = {
    Future {
      db withSession { implicit session =>
        pictureRepo.retrievePicsByAccountId(accountId)
      }
    }
  }

  def authenticateAccount(signIn: SignIn, ipAddress: Option[RemoteAddress]): Future[Either[AuthenticationFailure, Account]] = {
    Future {
      db withSession { implicit session =>
        accountRepo.retrieveAccountByEmail(signIn.email) match {
          case Some(account) if accountUnlocked(account) =>
            if (Password.verify(signIn.password, account.password)) {
              val updatedAccount = updateAccountSignInSuccess(account, ipAddress)
              accountRepo.updateAccount( updatedAccount )
              Right(updatedAccount)
            } else {
              accountRepo.updateAccount( updateAccountSignInFailure(account, ipAddress)  ) // "The password provided is incorrect."
              Left(WrongPassword(account.email))
            }
          case Some(account) => Left(AccountLocked(account.email)) //"The account is locked; please try again in a few moments."
          case _ => Left(UserDoesNotExist)
        }
      }
    }
  }

  def updateAccountPassword(accountId: Long, passwordUpdate: PasswordUpdate): Future[Boolean] =
    Future {
      db withSession { implicit session =>
        accountRepo.retrieveAccount(accountId) match {
          case Some(account) if Password.verify(passwordUpdate.password, account.password)  =>
            accountRepo.updateAccountPassword(accountId, passwordUpdate.newPassword)
          case _ => false
        }
      }
    }

  private def accountUnlocked(account:Account) = account.lockedOutUntil.map(_.isBefore(LocalDateTime.now())).getOrElse(true)

  // Update account to reflect a successful sign in attempt
  private def updateAccountSignInSuccess(account:Account,ipAddress:Option[RemoteAddress]) : Account = account.copy(
    loginCount = account.loginCount+1
    ,failedLoginCount = 0
    , lastLoginAt = account.currentLoginAt
    , currentLoginAt = Some(LocalDateTime.now())
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
  )

  // Update account to reflect a failed sign in attempt
  private def updateAccountSignInFailure(account:Account,ipAddress:Option[RemoteAddress]) : Account = account.copy(
    failedLoginCount = account.failedLoginCount+1
    , lockedOutUntil = if (siteSettings.accountLockout && siteSettings.accountLockoutMaxAttempts < account.failedLoginCount + 1) Some(LocalDateTime.now.plus(siteSettings.accountLockoutPeriod * 1000, ChronoUnit.MILLIS)) else None
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
  )

  // Update account to reflect a failed sign in attempt using a remember-me cookie
  private def updateAccountRememberMeFailure(account:Account,ipAddress:Option[RemoteAddress],lock:Boolean) : Account = account.copy(
    failedLoginCount = account.failedLoginCount+1
    , lockedOutUntil = if (lock || (siteSettings.accountLockout && siteSettings.accountLockoutMaxAttempts < account.failedLoginCount + 1)) Some(LocalDateTime.now.plus(siteSettings.accountLockoutPeriod * 1000, ChronoUnit.MILLIS)) else None
    , lastLoginIp = account.currentLoginIp
    , currentLoginIp = ipAddress
  )

}
