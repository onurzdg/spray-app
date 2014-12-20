package component.chat.actor

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import spray.can.Http
import spray.http._
import spray.routing.RequestContext

import scala.concurrent.duration._

private[chat]
object SSEActor {
  sealed trait SSEMessage
  case object SSEEnd extends SSEMessage
  case object SSEClose extends SSEMessage
  case class SSEEvent(data:List[String], id:Option[String] = None,event:Option[String] = None,retry: Option[Long] = None) extends SSEMessage {
    override def toString = {
      val idStr  = id.map(id => s"id:$id\n").getOrElse("")
      val evtStr = event.map(evt => s"event:$evt\n").getOrElse("")
      val retryStr = retry.map(t => s"retry:$t\n").getOrElse("")
      val dataStr = data.map({x => s"data:$x"}).reduce(_ + "\n" + _)
      s"${idStr}${evtStr}${retryStr}${dataStr}\n\n"
    }
  }

  /*
   * Using a companion object props() factory method to create the in‐
   * stance of Props for an Akka actor to avoid closing over a reference to this
   * from the actor that is creating the new actor instance.
   */

  def props(ctx: RequestContext): Props = Props(new SSEActor(ctx))
}

private[chat]
class SSEActor(ctx:RequestContext) extends Actor with ActorLogging {
  import SSEActor._
  val comment = ":\n\n"
  ctx.responder ! ChunkedResponseStart(HttpResponse(entity = comment))
  context.setReceiveTimeout(20.seconds)

  def receive: Receive = {
    case evt @ SSEEvent(_,_,_,_) =>
      log.debug(s"Sending SSE event: ${evt.toString}")
      ctx.responder ! MessageChunk(evt.toString)

    case ReceiveTimeout =>
      ctx.responder ! MessageChunk(comment)

    case SSEEnd =>
      ctx.responder ! ChunkedMessageEnd
      context.stop(self)

    case SSEClose =>
        // notify client to stop retrying
       ctx.responder ! StatusCodes.NotFound
       context.stop(self)

    case ev: Http.ConnectionClosed =>
      log.info(s"Stopping SSE stream, reason: signed out")
      context.stop(self)
  }
}
