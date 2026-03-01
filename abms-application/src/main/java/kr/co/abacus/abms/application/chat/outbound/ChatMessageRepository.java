package kr.co.abacus.abms.application.chat.outbound;

import java.util.List;

import kr.co.abacus.abms.domain.chat.ChatMessage;
import kr.co.abacus.abms.domain.chat.ChatSession;

public interface ChatMessageRepository {

    ChatMessage save(ChatMessage message);

    List<ChatMessage> findAllByChatSessionAndDeletedFalseOrderByTimestampAsc(ChatSession session);

}
