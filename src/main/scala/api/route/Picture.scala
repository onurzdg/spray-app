package api.route

import api.CommonTraits
import spray.http._
import api.ApiRoot

private[api]
trait Picture { self: CommonTraits =>

  def pictureRoute = {
    pathPrefix(ApiRoot) {
      pathPrefix("picture") {
        path(LongNumber) { pictureId =>
          rejectEmptyResponse {
            complete(accountService.retrievePictureById(pictureId))
          }
        } ~ path("account"/IntNumber) { accountId =>
          onSuccess(accountService.retrievePictureByAccountId(accountId)) {
            case l@ List(_, _*) => complete(StatusCodes.OK, l)
            case _ =>  complete(StatusCodes.NotFound)
          }
        }
      }
    }
  }
}


