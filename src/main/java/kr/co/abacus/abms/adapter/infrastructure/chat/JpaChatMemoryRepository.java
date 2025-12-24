package kr.co.abacus.abms.adapter.infrastructure.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.domain.chat.ChatMemoryMessage;

@Repository
@RequiredArgsConstructor
@Transactional
public class JpaChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMemoryMessageJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> findConversationIds() {
        return jpaRepository.findDistinctConversationIds();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(String conversationId) {
        List<ChatMemoryMessage> entities = jpaRepository.findByConversationIdOrderByIdAsc(conversationId);
        List<Message> messages = new ArrayList<>();

        for (ChatMemoryMessage entity : entities) {
            Message message = toMessage(entity);
            messages.add(message);
        }

        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        List<ChatMemoryMessage> existingMessages = jpaRepository.findByConversationIdOrderByIdAsc(conversationId);
        int existingCount = existingMessages.size();

        // If messages were removed (e.g. window size limit), re-sync completely
        if (existingCount > messages.size()) {
            jpaRepository.deleteByConversationId(conversationId);
            existingCount = 0;
        }

        // Only save new messages
        if (messages.size() > existingCount) {
            List<Message> newMessages = messages.subList(existingCount, messages.size());
            List<ChatMemoryMessage> entitiesToSave = new ArrayList<>();

            for (Message message : newMessages) {
                entitiesToSave.add(new ChatMemoryMessage(
                        conversationId,
                        message.getMessageType(),
                        message.getText()));
            }
            jpaRepository.saveAll(entitiesToSave);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        jpaRepository.deleteByConversationId(conversationId);
    }

    private Message toMessage(ChatMemoryMessage entity) {
        String content = entity.getContent();
        MessageType type = entity.getMessageType();

        return switch (type) {
            case USER -> new UserMessage(content);
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM -> new SystemMessage(content);
            default -> new UserMessage(content);
        };
    }

}
