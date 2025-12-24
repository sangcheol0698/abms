package kr.co.abacus.abms.application.chat.dto.query;

import java.time.LocalDateTime;

public record ChatSessionSummary(
        Long id,
        String sessionId,
        String title,
        Boolean favorite,
        LocalDateTime updatedAt) {

}
