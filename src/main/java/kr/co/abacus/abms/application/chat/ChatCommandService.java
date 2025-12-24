package kr.co.abacus.abms.application.chat;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import kr.co.abacus.abms.application.chat.dto.command.ChatSendCommand;
import kr.co.abacus.abms.application.chat.dto.query.ChatStreamResult;
import kr.co.abacus.abms.application.chat.outbound.ChatSessionRepository;
import kr.co.abacus.abms.domain.chat.ChatSession;

@Service
@Transactional
public class ChatCommandService {

    private static final String SYSTEM_PROMPT = """
            당신은 ABMS(Abacus Business Management System)의 인사/조직 관리 시스템 AI 어시스턴트입니다.

            주요 기능:
            - 직원 정보 조회 (이름, 부서, 직급, 등급, 상태, 입사일, 생년월일)
            - 부서 정보 조회 (부서명, 부서코드, 부서유형, 부서장)
            - 조직도 조회 (상위/하위 부서 구조)

            사용 가능한 도구(Tools):
            - getEmployeeInfo: 직원 이름으로 직원 정보 조회
            - getDepartmentInfo: 부서명으로 부서 정보 및 부서장 조회
            - getSubDepartments: 특정 부서의 하위 부서 목록 조회

            응답 가이드라인:
            1. 정중하고 전문적인 어조 사용 (존칭: ~님)
            2. 필요한 정보를 먼저 도구로 조회한 후 응답
            3. 조회된 데이터를 기반으로 정확한 정보 제공
            4. 정보가 부족할 경우 추가 질문
            5. 한국어로 응답

            제한 사항:
            - 제공된 도구만 사용하여 데이터 조회
            - 없는 정보는 만들지 않고 "정보를 찾을 수 없습니다" 응답
            - 직원/부서 정보 외의 질문은 도구를 사용하지 않고 일반적으로 답한다.
            """;

    private final ChatClient chatClient;
    private final EmployeeInfoTools employeeInfoTools;
    private final OrganizationTools organizationTools;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTitleService chatTitleService;

    public ChatCommandService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            EmployeeInfoTools employeeInfoTools,
            OrganizationTools organizationTools,
            ChatSessionRepository chatSessionRepository,
            ChatTitleService chatTitleService) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.employeeInfoTools = employeeInfoTools;
        this.organizationTools = organizationTools;
        this.chatSessionRepository = chatSessionRepository;
        this.chatTitleService = chatTitleService;
    }

    public Mono<ChatStreamResult> streamMessage(ChatSendCommand command) {
        return Mono.fromCallable(() -> {
            boolean isNewSession = isNewSession(command.sessionId());
            String conversationId = getOrCreateConversationId(command.sessionId());
            return new SessionInfo(isNewSession, conversationId);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .map(sessionInfo -> {
                    boolean isNewSession = sessionInfo.isNewSession();
                    String conversationId = sessionInfo.conversationId();

                    // Create a sink for tool call events
                    Sinks.Many<String> toolCallSink = Sinks.many().multicast().onBackpressureBuffer();

                    // Set tool call notifier on tools
                    employeeInfoTools.setToolCallNotifier(toolName -> toolCallSink.tryEmitNext(toolName));
                    organizationTools.setToolCallNotifier(toolName -> toolCallSink.tryEmitNext(toolName));

                    Flux<String> contentStream = chatClient.prompt()
                            .user(command.content())
                            .tools(employeeInfoTools, organizationTools)
                            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                            .stream()
                            .content()
                            .doOnComplete(() -> {
                                // Clear notifiers and complete sink
                                employeeInfoTools.setToolCallNotifier(null);
                                organizationTools.setToolCallNotifier(null);
                                toolCallSink.tryEmitComplete();
                            })
                            .doOnError(e -> {
                                employeeInfoTools.setToolCallNotifier(null);
                                organizationTools.setToolCallNotifier(null);
                                toolCallSink.tryEmitError(e);
                            });

                    // Generate title asynchronously for new sessions
                    if (isNewSession) {
                        chatTitleService.generateTitleAsync(conversationId, command.content());
                    }

                    return new ChatStreamResult(conversationId, contentStream, toolCallSink.asFlux());
                });
    }

    public String sendMessage(ChatSendCommand command) {
        boolean isNewSession = isNewSession(command.sessionId());
        String conversationId = getOrCreateConversationId(command.sessionId());

        String response = chatClient.prompt()
                .user(command.content())
                .tools(employeeInfoTools, organizationTools)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        // Generate title asynchronously for new sessions
        if (isNewSession) {
            chatTitleService.generateTitleAsync(conversationId, command.content());
        }

        return response != null ? response : "";
    }

    private boolean isNewSession(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return true;
        }
        return chatSessionRepository.findBySessionIdAndDeletedFalse(sessionId).isEmpty();
    }

    private String getOrCreateConversationId(@Nullable String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            String newSessionId = UUID.randomUUID().toString();
            ChatSession newSession = ChatSession.create("새로운 대화", newSessionId);
            chatSessionRepository.save(newSession);
            return newSessionId;
        }

        // Ensure session exists in our metadata table
        chatSessionRepository.findBySessionIdAndDeletedFalse(sessionId)
                .orElseGet(() -> chatSessionRepository.save(ChatSession.create("새로운 대화", sessionId)));

        return sessionId;
    }

    private record SessionInfo(boolean isNewSession, String conversationId) {
    }

    public void toggleFavorite(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
        session.toggleFavorite();
        chatSessionRepository.save(session);
    }

}
