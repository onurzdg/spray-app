package api.route

import component.account.domain._
import html._
import api.domain.SessionCookie
import api.{ApiRoot, CommonTraits}
import spray.http.HttpHeaders.Location
import spray.http.{MediaTypes, StatusCodes}

private[api]
trait Public {self: CommonTraits =>

  val publicRoutes = {
    get {
      pathPrefixTest("signin" | "signup") {
        (sessionCookie | rememberMeCookie) { session =>
          redirect("/", StatusCodes.TemporaryRedirect)   // do not show signin or signup page if the user is already logged-in
        }
      } ~ path("signin") {
         complete {
          page(signin(generateLoginCsrfToken("loginToken")))
        }
      } ~ path("signup") {
         complete {
          page(signup())
        }
      }
    } ~ pathPrefix(ApiRoot) {
      respondWithMediaType(MediaTypes.`application/json`) {
        (path("signup") & post) {
          entity(as[SignUp]) { signUp =>
            onSuccess(accountService.createAccount(signUp)) {
              case Left(failure) => complete(StatusCodes.Conflict,
                "The email address you provided is already registered to another account")
              case Right(acc) =>
                respondWithHeader(Location(s"/$ApiRoot/account/${acc.id.get}")) {
                  setSession(SessionCookie(data = Map("id" -> acc.id.get.toString), path = Some("/"))) {
                    complete(StatusCodes.Created, acc)
                  }
                }
            }
          }
        } ~ (path("signin") & post) {
          entity(as[SignIn]) { signIn =>
            verifyLoginCsrfToken {
              optionalClientIP { ipOpt =>
                authenticateUser(signIn, ipOpt) { account =>
                  val sessionCookie =
                    SessionCookie(data = Map("id" -> account.id.get.toString), path = Some("/"))
                  setSession(sessionCookie) {
                    complete(account)
                    if (signIn.rememberMe) { // Set remember-me cookie *if* it was set on the sign-in form
                      setRememberMe(sessionCookie) {
                        complete(account)
                      }
                    }
                    else complete( account)
                  }
                }
              }
            }
          }
        } 
      }
    }
  }
}
