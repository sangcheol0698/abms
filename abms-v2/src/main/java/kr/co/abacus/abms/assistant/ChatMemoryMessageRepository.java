package kr.co.abacus.abms.assistant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMemoryMessageRepository extends JpaRepository<ChatMemoryMessage, Long> {

    List<ChatMemoryMessage> findAllByConversationIdOrderBySeqAsc(String conversationId);

    @Query("select distinct m.conversationId from ChatMemoryMessage m")
    List<String> findConversationIds();

    @Modifying
    @Query("delete from ChatMemoryMessage m where m.conversationId = :conversationId")
    void deleteByConversationId(String conversationId);

}
