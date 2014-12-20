package component.chat.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import scaldi.Injector
import spray.json._
import spray.routing.RequestContext

import scala.collection.mutable.ArrayBuffer

object Chat {
  case class ChatMessage(message:String)
  case class AddListener(ctx:RequestContext)

  object ChatMessage extends ChatMessageJsonProtocol
  trait ChatMessageJsonProtocol extends DefaultJsonProtocol {
    implicit val messageFormat = jsonFormat1(ChatMessage.apply)
  }

}

class Chat(implicit inj: Injector) extends Actor with ActorLogging {
  import Chat._
  log.info("Starting chat actor.")
  val watched = ArrayBuffer.empty[ActorRef]

  def receive : Receive = {

    case AddListener(ctx) =>
      log.info(s"Adding SSE listener.")
      val listener = context.actorOf(SSEActor.props(ctx))
      context.watch(listener)
      watched += listener

    case msg @ ChatMessage(_) =>
      log.info(s"Received chat message.")
      watched.foreach(_ ! SSEActor.SSEEvent(event=Some("message"),data=List(msg.toJson.compactPrint)))

    case Terminated(listener) =>
      watched -= listener

  }
}
