package kr.co.abacus.abms.adapter.web.chat.dto;

import java.time.LocalDateTime;
import java.util.List;

import kr.co.abacus.abms.application.chat.dto.query.ChatSessionDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;

public record ChatSessionResponse(
        Long id,
        String sessionId,
        String title,
        Boolean favorite,
        List<ChatMessageResponse> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ChatSessionResponse from(ChatSessionSummary summary) {
        return new ChatSessionResponse(
                summary.id(),
                summary.sessionId(),
                summary.title(),
                summary.favorite(),
                List.of(),
                summary.updatedAt(),
                summary.updatedAt());
    }

    public static ChatSessionResponse from(ChatSessionDetail detail) {
        List<ChatMessageResponse> messages = detail.messages().stream()
                .map(ChatMessageResponse::from)
                .toList();

        return new ChatSessionResponse(
                detail.id(),
                detail.sessionId(),
                detail.title(),
                detail.favorite(),
                messages,
                detail.createdAt(),
                detail.updatedAt());
    }

}
