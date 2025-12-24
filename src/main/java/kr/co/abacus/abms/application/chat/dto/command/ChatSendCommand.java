package kr.co.abacus.abms.application.chat.dto.command;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.chat.ChatMessage;
import kr.co.abacus.abms.domain.chat.ChatSession;

public record ChatSendCommand(
        @Nullable String sessionId,
        String content) {

    public ChatMessage toUserMessage(ChatSession session) {
        return ChatMessage.createUserMessage(content, session);
    }

}
