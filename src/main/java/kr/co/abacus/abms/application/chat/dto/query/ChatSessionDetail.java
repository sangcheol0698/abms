package kr.co.abacus.abms.application.chat.dto.query;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionDetail(
        Long id,
        String sessionId,
        String title,
        Boolean favorite,
        List<ChatMessageDetail> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
