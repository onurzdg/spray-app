package component.account.test


import component.account.AccountService
import component.account.dao.{Accounts, Pictures}
import component.account.domain.Password.ClearTextPassword
import component.account.domain._
import concurrency.ExecutionContexts
import configuration.SiteSettings
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import scaldi.Injectable

import scala.language.higherKinds
import scala.slick.jdbc.JdbcBackend.Database

class AccountServiceSpec extends FlatSpec with Matchers with ScalaFutures with Injectable {
  implicit val dbExecutor = ExecutionContexts.dbExecutionContext
  implicit val injector = module.Bindings.testInjector
  implicit val patienceConf = PatienceConfig(Span(1, Seconds))

  val db: Database = inject[Database]
  val email = Email("tars@interstellar.com")
  val originalPass = ClearTextPassword("123456")
  val account =
    Account(name = Name("Tars"),
      email = email, password = originalPass)

  val service = new AccountService(db, new Accounts, new Pictures, SiteSettings())

  "User creation" should "succeed" in {
    whenReady(service.createAccount(account)) {
      case Left(AccountCreationFailure(mail)) => fail(s"failed to create account for email address ${mail.email}")
      case Right(acc) =>  acc.id.isDefined should be(true); acc.email should be (email)
    }
  }
  
  "Authentication with correct credentials " should "succeed" in {
    whenReady(service.authenticateAccount(SignIn(email, originalPass, false), None)) {
      case Left(failure) => fail("failed to authenticate with correct credentials")
      case Right(acc) => acc.email should be (email)
    }
  }

  "Updating user password with correct old password " should "succeed " in {
    whenReady(service.retrieveAccountByEmail(email)) {
      case Some(acc) =>
        whenReady(service.updateAccountPassword(acc.id.get, PasswordUpdate(originalPass, ClearTextPassword("234567")))) { success =>
          success should be (true)
        }
      case None => fail(s"no user with email ${email.email} exists")
    }
  }

  "Updating user password with wrong old password" should "fail" in { // pass changed above
    whenReady(service.retrieveAccountByEmail(email)) {
      case Some(acc) =>
        whenReady(service.updateAccountPassword(acc.id.get, PasswordUpdate(originalPass, ClearTextPassword("345678")))) { success =>
          success should be (false)
        }
      case None => fail(s"no user with email ${email.email} exists")
    }
  }

  it should "throw IllegalArgumentException if password has less than 6 characters " in {
    whenReady(service.retrieveAccountByEmail(email)) {
      case Some(acc) => a [IllegalArgumentException] should be thrownBy {
        whenReady(service.updateAccountPassword(acc.id.get, PasswordUpdate(ClearTextPassword("123456"), ClearTextPassword("23456")))) { res => }
      }
      case None => fail(s"no user with email ${email.email} exists")
    }
  }

  "Retrieving the user with the same email address" should "yield the same user " in {
    whenReady(service.retrieveAccountByEmail(email)) {
      case Some(acc) => acc.email should be (Email("tars@interstellar.com"))
      case _ => fail("could not retrieve user with email address " + email.email)
    }
  }

  "Attempting to create a user with the same email address" should "be rejected" in {
    whenReady(service.createAccount(account)) {
      case Left(failure) => failure.email should be(email)
      case Right(acc) =>  acc.id.isDefined should be(true) ; acc.email should be (email)
    }
  }

  "Deleting the user" should "succeed " in {
    whenReady(service.retrieveAccountByEmail(email).map{_.get.id}) {
      case Some(id) =>
        whenReady(service.deleteAccount(id)) { res =>
          res should be (true)
        }
      case None => fail(s"no user with email ${email.email} exists")
    }
  }
}