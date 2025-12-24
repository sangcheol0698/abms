package kr.co.abacus.abms.application.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.application.chat.outbound.ChatSessionRepository;

@Slf4j
@Service
public class ChatTitleService {

    private static final String TITLE_PROMPT = """
            주어진 사용자 메시지를 바탕으로 이 대화의 제목을 5단어 이내로 작성해주세요.
            제목만 출력하고 다른 설명은 하지 마세요.
            
            사용자 메시지: %s
            """;

    private final ChatClient chatClient;
    private final ChatSessionRepository chatSessionRepository;

    public ChatTitleService(
            ChatClient.Builder chatClientBuilder,
            ChatSessionRepository chatSessionRepository) {
        this.chatClient = chatClientBuilder.build();
        this.chatSessionRepository = chatSessionRepository;
    }

    @Async
    @Transactional
    public void generateTitleAsync(String sessionId, String userMessage) {
        try {
            String response = chatClient.prompt()
                    .user(String.format(TITLE_PROMPT, userMessage))
                    .call()
                    .content();

            if (response != null && !response.isBlank()) {
                String title = response.trim();
                if (title.length() > 50) {
                    title = title.substring(0, 47) + "...";
                }

                final String finalTitle = title;
                chatSessionRepository.findBySessionIdAndDeletedFalse(sessionId)
                        .ifPresent(session -> {
                            session.updateTitle(finalTitle);
                            chatSessionRepository.save(session);
                            log.info("Generated title for session {}: {}", sessionId, finalTitle);
                        });
            }
        } catch (Exception e) {
            log.warn("Failed to generate title for session {}: {}", sessionId, e.getMessage());
        }
    }

}
