package kr.co.abacus.abms.assistant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findAllByAccountIdOrderByFavoriteDescLastMessageAtDesc(Long accountId);

}
