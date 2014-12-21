package api

import akka.event.Logging._
import spray.http.StatusCodes._
import spray.http._
import spray.routing.directives.LogEntry

private[api]
trait HttpLogger {self: CommonTraits =>

  def logErrorResponses(request: HttpRequest): Any ⇒ Option[LogEntry] = {
    case HttpResponse(code@ (OK | NotModified | PartialContent | Accepted), resposne, _, _) ⇒
      Some(LogEntry("Request : " + request +  "\n Response: " +  code , InfoLevel))
    case HttpResponse(NotFound, _, _, _) ⇒ Some(LogEntry(NotFound + " " + request.uri, WarningLevel))
    case HttpResponse(BadRequest, _, _, _) ⇒ Some(LogEntry(BadRequest + " " + request.uri, InfoLevel))
    case HttpResponse(InternalServerError, _, _, _) ⇒ Some(LogEntry(InternalServerError + " " + request.uri, ErrorLevel))
    case r @ HttpResponse(Found | MovedPermanently, _, _, _) ⇒
      Some(LogEntry(s"${r.status.intValue}: ${request.uri} -> ${r.header[HttpHeaders.Location].map(_.uri.toString).getOrElse("")}", WarningLevel))
    case r @ ChunkedResponseStart(response) ⇒ Some(
      LogEntry("Chat request\n Request : " + request + "\n Response: " + response, InfoLevel))
    case r @ MessageChunk(body, _) ⇒ Some(
      LogEntry("Chat request\n Request : " + request + "\n Response: " + body, InfoLevel))
    case response ⇒ Some(
      LogEntry("Non-200 response for\n Request : " + request + "\n Response: " + response,  WarningLevel))
  }
}
