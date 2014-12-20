package api.route

import component.account.domain._
import api.{ApiRoot, CommonTraits}
import api.domain.{RequestResponse, SessionCookie}
import spray.http.HttpHeaders.Location
import spray.http.{DateTime, EntityTag, MediaTypes, StatusCodes}


private[api]
trait Account extends { self: CommonTraits =>

  import api.url.static._

  def accountSignUpRoute = {
    respondWithMediaType(MediaTypes.`application/json`) {
      (path(ApiRoot / "account") & post) {
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
      }
    }
  }

  def accountRoute(implicit session: SessionCookie) = {
    pathPrefix(ApiRoot) {
      respondWithMediaType(MediaTypes.`application/json`) {
         (path("signout") & post ) {
           val redirection = RequestResponse(true, None: Option[Int], Some(signIn), List.empty)
           optionalCookie(siteSettings.rememberCookieName) {
             case Some(cookie) =>
               deleteCookies() {
               complete {redirection}
             }
             case _ => deleteSessionCookie(path = root) {
               complete { redirection }
             }
           }
         } ~ pathPrefix("account"/LongNumber) { accId =>
           val accountId = session("id").toLong
           if(accId != accountId) {
             reject
           }
           get {
             onSuccess(accountService.retrieveAccount(accountId)) {
               case Some(acc) =>
                 conditional(EntityTag(acc.hashCode().toString), DateTime.now) {
                   complete(acc)
                 }
               case _ => reject
             }
           } ~ put {
             path("name") {
               entity(as[Name]) { newName =>
                 onSuccess(accountService.updateAccountName(accountId,newName)){ updated =>
                   if(updated) complete(StatusCodes.NoContent) else complete(StatusCodes.BadRequest)
                 }
               }
             } ~ path("email") {
               entity(as[Email]) { newEmail =>
                 accountService.updateAccountEmail(accountId,newEmail)
                 complete(StatusCodes.NoContent)
               }
             } ~ path("password") {
               entity(as[PasswordUpdate]) { passwordUpdate =>
                 onSuccess(accountService.updateAccountPassword(accountId, passwordUpdate)) { updated =>
                   if(updated) complete(StatusCodes.NoContent) else complete(StatusCodes.BadRequest)
                 }
               }
             }
           } ~ delete {
             onSuccess(accountService.deleteAccount(accountId)) { deleted =>
               if(deleted) complete(StatusCodes.NoContent) else complete(StatusCodes.BadRequest)
             }
           }
         }
      }
    }
  }
}