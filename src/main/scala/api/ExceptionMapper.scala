package api

import akka.pattern.{AskTimeoutException, CircuitBreakerOpenException}
import api.domain.RequestResponse
import spray.http.StatusCodes
import spray.routing.ExceptionHandler
import spray.util.LoggingContext

private[api]
trait ExceptionMapper { self: CommonTraits =>
  implicit def serverExceptionHandler(implicit log: LoggingContext) = {
    ExceptionHandler {
      case e: CircuitBreakerOpenException =>
        log.warning("Server is under heavy load")
        // Circuit breaker has flipped; additional requests will fail fast - let the user know they should probably wait..
        complete(StatusCodes.ServiceUnavailable,
          RequestResponse(false, None: Option[Int], None,
            List("We are experiencing a temporary problem with our servers; please wait and try again in a few minutes.")))

      case e: AskTimeoutException =>
        log.warning("unable to complete request timely")
        complete(StatusCodes.ServiceUnavailable)

      case e: IllegalArgumentException =>
        log.warning(e.getMessage)
        complete(StatusCodes.BadRequest)
      case e: Exception =>
        log.error(e, e.getMessage)
        complete(StatusCodes.InternalServerError, RequestResponse(false, None: Option[Int],
          None, List("There is a problem with our servers, please wait and try again in a few minutes.")))
    }
  }

}
