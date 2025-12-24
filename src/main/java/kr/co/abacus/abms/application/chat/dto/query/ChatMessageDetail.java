package kr.co.abacus.abms.application.chat.dto.query;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.chat.ChatMessage;

public record ChatMessageDetail(
        @Nullable Long id,
        String role,
        String content,
        @Nullable LocalDateTime timestamp) {

    public static ChatMessageDetail from(ChatMessage message) {
        return new ChatMessageDetail(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getTimestamp());
    }

}
