package component

import component.chat.actor.Chat
import scaldi.Module

package object chat {
  class ChatModule extends Module {
    val `chat-service` = "chat-service"
    binding toProvider new Chat()
  }
}