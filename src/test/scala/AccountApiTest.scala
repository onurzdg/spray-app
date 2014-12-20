import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import component.security.crypto.CryptoService
import module.Bindings
import configuration.SiteSettings


import api.route.Public
import api.{Routes, CommonTraits}
import org.specs2.mutable.Specification
import scaldi.{Injectable, Injector}
import spray.http.HttpHeaders.RawHeader
import spray.httpx.encoding.{NoEncoding, Deflate, Gzip}
import spray.json.JsValue
import spray.routing.HttpService
import spray.testkit._
import spray.testkit.RouteTest
import spray.http._
import StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import component.account.domain._
import component.account.domain.Password.ClearTextPassword
import org.scalatest._
import spray.http.HttpHeaders._
import HttpEncodings._
import spray.json._
import spray.json.lenses.JsonLenses._
import Name._

import scala.slick.jdbc.JdbcBackend._


class AccountApiTest extends FlatSpec with ScalatestRouteTest with Matchers with TestBase with CommonTraits with Routes with Injectable {
   def actorRefFactory: ActorRefFactory = system
   val urlPrefix = "https://localhost"


  val cryptoService = inject[CryptoService]

  val userAccount = Account(name = Name("Tars"),
    email = Email("tars@interstellar.com"),
    password = ClearTextPassword("222222")
  )

  val validAccountJsonLogin = """{"email":"tars@interstellar.com", "password":"222222", "rememberMe": false }"""
  val invalidAccountJsonLogin = """{"email":"tars@interstellar.com", "password":"zzzzzz", "rememberMe": false }"""
  val validAccountCreationJson= """{"name":"Tars", "email":"tars@interstellar.com", "password":"222222"}"""
  val loginCsfrToken =  "2sNmhxG88/lqbIr4i96AbIMjoFQ=-bsuYZQVhSeLJNmwVCttNbA==-IQX49jRNdIcp6CwqnYeZdZHOAaLqoTMNshj6dyjEDos="


  "The public API /sigin" should "be reachable without login" in {
    Get(s"$urlPrefix/signin") ~> sealRoute(routes) ~> check {
      status shouldBe OK
    }
  }

  "The public API /signup" should "be reachable without login" in {
    Get(s"$urlPrefix/signup") ~> sealRoute(routes) ~> check {
      status shouldBe OK
    }
  }

  "/api/signin" should "produce bad request when provided with a password less than 6 characters" in {
    Post(s"$urlPrefix/api/signin", HttpEntity(MediaTypes.`application/json`,
      """{"email":"tars@interstellar.com", "password":"zzzzz", "rememberMe": false }""")) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe BadRequest
        removeSpaceAndControlChars(responseAs[String]) shouldBe
          removeSpaceAndControlChars("""{"success": false, "errors": ["requirement failed: Password should contain at least six characters"]}""")
      }
  }

  "/api/signin" should "produce bad request without loginCsfrToken" in {
    Post(s"$urlPrefix/api/signin", HttpEntity(MediaTypes.`application/json`,
        invalidAccountJsonLogin)) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe BadRequest
        removeSpaceAndControlChars(responseAs[String]) shouldBe
          removeSpaceAndControlChars("""{"success": false, "errors": ["send the header loginCsfrToken"]}""")
      }
  }

  "/api/signin" should "produce OK with loginCsfrToken and fail to login with wrong credentials" in {
    Post(s"$urlPrefix/api/signin", HttpEntity(MediaTypes.`application/json`, invalidAccountJsonLogin)) ~>
      addHeader("loginCsfrToken", loginCsfrToken) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe OK
        removeSpaceAndControlChars(Gzip.decode(response).entity.asString) shouldBe
          removeSpaceAndControlChars("""{"success": false, "errors": ["Credentials are wrong"]}""")
      }
  }


  "/api/signup" should "produce Created " in {
    Post(s"$urlPrefix/api/signup", HttpEntity(MediaTypes.`application/json`, validAccountCreationJson)) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        val resourceUrl = header("Location").get.value
        resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1).forall(_.isDigit) shouldBe true
        status shouldBe Created
      }
  }

  def signedInHeaders = {
      addHeader("sessionCsfrToken", sessionCsfrToken) ~>  addHeader("Cookie", cookieValue)
  }

  var accountId = 0L;
  var sessionCsfrToken = "";
  var cookieValue = ""
  "/api/signin" should "produce OK with loginCsfrToken and successfuly login with correct credentials" in {
    Post(s"$urlPrefix/api/signin", HttpEntity(MediaTypes.`application/json`, validAccountJsonLogin)) ~>
      addHeader("loginCsfrToken", loginCsfrToken) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        val cookie: HttpCookie = header[`Set-Cookie`].get.cookie
        status shouldBe OK
        val account: Account = Gzip.decode(response).entity.asString.parseJson.convertTo[Account]
        account.email.email shouldBe "tars@interstellar.com"
        accountId = account.id.get
        cookieValue = cookie.name + "=" + cookie.content
        sessionCsfrToken = cryptoService.encryptAndSignTimeSensitiveData(account.id.get.toString)
      }
  }

  "Attempt to create another user with the same email address" should "produce Conflict" in {
    Post(s"$urlPrefix/api/signup", HttpEntity(MediaTypes.`application/json`, validAccountCreationJson)) ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe Conflict
    }
  }

  "Accessing account info" should "produce OK" in {
    Get(s"$urlPrefix/api/account/$accountId") ~>
      signedInHeaders ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe OK
        val account = Gzip.decode(response).entity.asString.parseJson.convertTo[Account]
        account.id.get shouldBe accountId
        account.email shouldBe userAccount.email
        account.name shouldBe userAccount.name
      }
  }

  "Updating user name" should "produce NoContent" in {
    Put(s"$urlPrefix/api/account/$accountId/name", Name("Slick")) ~>
      signedInHeaders ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe NoContent
      }
  }

  "Updating user email" should "produce NoContent" in {
    Put(s"$urlPrefix/api/account/$accountId/email", Email("tars3@interstellar.com")) ~>
      signedInHeaders ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe NoContent
      }
  }

  "Updating user password with wrong credentials" should "produce Bad Request" in {
    Put(s"$urlPrefix/api/account/$accountId/password",
      HttpEntity(MediaTypes.`application/json`, """{"password":"222221","newPassword":"222223"}""")) ~>
      signedInHeaders ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe BadRequest
      }
  }

  "Updating user password with correct credentials" should "produce NoContent" in {
    Put(s"$urlPrefix/api/account/$accountId/password", HttpEntity(MediaTypes.`application/json`,
      """{"password":"222222","newPassword":"222223"}""")) ~>
      signedInHeaders ~>
      encode(Gzip) ~>
      sealRoute(routes) ~>
      check {
        status shouldBe NoContent
      }
  }

  "Deleting a user account" should "produce NoContent" in {
    Delete(s"$urlPrefix/api/account/$accountId") ~>
      signedInHeaders ~>
      sealRoute(routes) ~>
      check {
        status shouldBe NoContent
      }
  }


}


