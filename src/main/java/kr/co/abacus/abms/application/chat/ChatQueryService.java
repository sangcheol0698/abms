package kr.co.abacus.abms.application.chat;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.chat.dto.query.ChatMessageDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;
import kr.co.abacus.abms.application.chat.outbound.ChatSessionRepository;
import kr.co.abacus.abms.domain.chat.ChatSession;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMemoryRepository chatMemoryRepository;

    public List<ChatSessionSummary> getRecentSessions(int limit) {
        return chatSessionRepository.findRecentSessions(limit);
    }

    public List<ChatSessionSummary> getFavoriteSessions() {
        return chatSessionRepository.findAllByFavoriteTrueAndDeletedFalseOrderByUpdatedAtDesc()
                .stream()
                .map(session -> new ChatSessionSummary(
                        session.getIdOrThrow(),
                        session.getSessionId(),
                        session.getTitle(),
                        session.getFavorite(),
                        session.getUpdatedAt()))
                .toList();
    }

    public ChatSessionDetail getSessionDetail(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // Read messages from ChatMemory (Spring AI)
        List<Message> memoryMessages = chatMemoryRepository.findByConversationId(sessionId);
        List<ChatMessageDetail> messages = memoryMessages.stream()
                .map(this::toMessageDetail)
                .toList();

        return new ChatSessionDetail(
                session.getIdOrThrow(),
                session.getSessionId(),
                session.getTitle(),
                session.getFavorite(),
                messages,
                session.getCreatedAt(),
                session.getUpdatedAt());
    }

    private ChatMessageDetail toMessageDetail(Message message) {
        String role = message.getMessageType() == MessageType.USER ? "USER" : "ASSISTANT";
        return new ChatMessageDetail(null, role, Objects.requireNonNull(message.getText()), null);
    }

}
