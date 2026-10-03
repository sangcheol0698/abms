package kr.co.abacus.abms.assistant;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL 에 대화 메모리를 저장하는 Spring AI ChatMemoryRepository.
 * 텍스트 메시지(사용자/어시스턴트/시스템)만 보관한다.
 */
@Component
@Transactional
public class JpaChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMessageRepository repository;

    public JpaChatMemoryRepository(ChatMemoryMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findConversationIds() {
        return repository.findConversationIds();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(String conversationId) {
        List<Message> messages = new ArrayList<>();
        for (ChatMemoryMessage m : repository.findAllByConversationIdOrderBySeqAsc(conversationId)) {
            switch (MessageType.valueOf(m.getMessageType())) {
                case USER -> messages.add(new UserMessage(m.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(m.getContent()));
                case SYSTEM -> messages.add(new SystemMessage(m.getContent()));
                default -> {
                    // 도구 호출 메시지는 저장하지 않는다.
                }
            }
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        repository.deleteByConversationId(conversationId);
        int seq = 0;
        List<ChatMemoryMessage> rows = new ArrayList<>();
        for (Message message : messages) {
            MessageType type = message.getMessageType();
            String text = message.getText();
            if (type == MessageType.TOOL || text == null || text.isBlank()) {
                continue;
            }
            rows.add(new ChatMemoryMessage(conversationId, seq++, type.name(), text));
        }
        repository.saveAll(rows);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

}
