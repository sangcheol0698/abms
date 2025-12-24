package kr.co.abacus.abms.application.chat.outbound;

import java.util.List;

import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;

public interface CustomChatSessionRepository {

    List<ChatSessionSummary> findRecentSessions(int limit);

}
