package kr.co.abacus.abms.application.chat.outbound;

import java.util.List;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.chat.ChatMessage;
import kr.co.abacus.abms.domain.chat.ChatSession;

public interface ChatMessageRepository extends Repository<ChatMessage, Long> {

    ChatMessage save(ChatMessage message);

    List<ChatMessage> findAllByChatSessionAndDeletedFalseOrderByTimestampAsc(ChatSession session);

}
