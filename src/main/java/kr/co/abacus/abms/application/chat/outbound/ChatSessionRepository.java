package kr.co.abacus.abms.application.chat.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.chat.ChatSession;

public interface ChatSessionRepository extends Repository<ChatSession, Long>, CustomChatSessionRepository {

    ChatSession save(ChatSession session);

    Optional<ChatSession> findById(Long id);

    Optional<ChatSession> findBySessionIdAndDeletedFalse(String sessionId);

    List<ChatSession> findAllByFavoriteTrueAndDeletedFalseOrderByUpdatedAtDesc();

    List<ChatSession> findAllByDeletedFalseOrderByUpdatedAtDesc();

}
