package kr.co.abacus.abms.adapter.infrastructure.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import kr.co.abacus.abms.domain.chat.ChatMemoryMessage;

public interface ChatMemoryMessageJpaRepository extends JpaRepository<ChatMemoryMessage, Long> {

    List<ChatMemoryMessage> findByConversationIdOrderByIdAsc(String conversationId);

    @Query("SELECT DISTINCT c.conversationId FROM ChatMemoryMessage c WHERE c.deleted = false ORDER BY c.conversationId")
    List<String> findDistinctConversationIds();

    @Modifying
    @Query("DELETE FROM ChatMemoryMessage c WHERE c.conversationId = :conversationId")
    void deleteByConversationId(String conversationId);

}
