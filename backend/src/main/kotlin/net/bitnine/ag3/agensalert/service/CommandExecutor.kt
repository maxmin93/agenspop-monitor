package net.bitnine.ag3.agensalert.service

import net.bitnine.ag3.agensalert.common.Constants
import net.bitnine.ag3.agensalert.model.chat.ChatMessage
import net.bitnine.ag3.agensalert.model.chat.MessageType
import net.bitnine.ag3.agensalert.storage.UserStorage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CommandExecutor {

    @Autowired
    private lateinit var userStorage: UserStorage

    // TODO it should be refactored with the command pattern
    public fun execute(message: ChatMessage): ChatMessage {
        return when (message.content) {
            "/users" -> ChatMessage(
                    type = MessageType.COMMAND_RESULT,
                    content = userStorage.getAllUsers().toString(),
                    sender = Constants.SERVER_SENDER
            )
            else -> ChatMessage(
                    type = MessageType.COMMAND_ERROR,
                    content = "${message.content} is not a valid command.",
                    sender = Constants.SERVER_SENDER
            )
        }
    }
}