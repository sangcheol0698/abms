package kr.co.abacus.abms.adapter.api.chat.dto;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.chat.dto.query.ChatMessageDetail;

public record ChatMessageResponse(
        @Nullable Long id,
        String role,
        String content,
        @Nullable LocalDateTime timestamp) {

    public static ChatMessageResponse from(ChatMessageDetail detail) {
        return new ChatMessageResponse(
                detail.id(),
                detail.role(),
                detail.content(),
                detail.timestamp());
    }

    public static ChatMessageResponse assistantResponse(String content) {
        return new ChatMessageResponse(null, "ASSISTANT", content, LocalDateTime.now());
    }

}
