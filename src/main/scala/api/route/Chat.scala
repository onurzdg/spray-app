package api.route

import component.chat.actor.{Chat => ChatActor}
import api.{ApiRoot, CommonTraits}
import api.domain.SessionCookie
import spray.http.CacheDirectives.`no-cache`
import spray.http.HttpHeaders.{Connection, `Cache-Control`}
import spray.http.{MediaType, MediaTypes, StatusCodes}


private[api]
trait Chat  { self: CommonTraits =>
  val chat = injectActorRef[ChatActor]("chat-service")
  private[this] val chatTimer = metrics.timer("chatTimer")

  def chatRoute(implicit session: SessionCookie) = {
    pathPrefix(ApiRoot) {
      (path("chat") & post) {
        chatTimer.time {
          entity(as[ChatActor.ChatMessage]) { msg =>
            chat ! msg
            complete(StatusCodes.Accepted)
          }
        }
      }
    } ~ (get & pathPrefix("streaming")) {
      respondAsEventStream {
        path("chat") { ctx =>
          chat ! ChatActor.AddListener(ctx)
        }
      }
    }
  }

  val `text/event-stream` = MediaType.custom("text/event-stream")
  MediaTypes.register(`text/event-stream`)

  def respondAsEventStream =
    respondWithHeader(`Cache-Control`(`no-cache`)) &
      respondWithHeader(`Connection`("Keep-Alive")) &
      respondWithMediaType(`text/event-stream`)
}
