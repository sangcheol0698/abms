package kr.co.abacus.abms.application.chat.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.chat.ChatSession;

public interface ChatSessionRepository extends CustomChatSessionRepository {

    ChatSession save(ChatSession session);

    Optional<ChatSession> findById(Long id);

    Optional<ChatSession> findBySessionIdAndDeletedFalse(String sessionId);

    List<ChatSession> findAllByFavoriteTrueAndDeletedFalseOrderByUpdatedAtDesc();

    List<ChatSession> findAllByDeletedFalseOrderByUpdatedAtDesc();

}
