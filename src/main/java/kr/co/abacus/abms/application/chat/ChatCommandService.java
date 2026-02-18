package kr.co.abacus.abms.application.chat;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
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
            - 직원/프로젝트 목록 검색
            - 거래처 검색 및 거래처별 프로젝트 조회
            - 부서 정보 조회 (부서명, 부서코드, 부서유형, 부서장)
            - 조직도 조회 (상위/하위 부서 구조)
            - 부서별 구성원 조회
            - 프로젝트 상세 조회
            - 대시보드 핵심 지표 조회
            - 월별 매출 집계 조회
            - 조직 전체 통계 조회
            
            사용 가능한 도구(Tools):
            - getEmployeeInfo: 직원 이름으로 직원 정보 조회
            - searchEmployees: 직원 목록 검색 (이름 키워드, 최대 20명)
            - getDepartmentInfo: 부서명으로 부서 정보 및 부서장 조회
            - getSubDepartments: 특정 부서의 하위 부서 목록 조회
            - getDepartmentMembers: 특정 부서의 소속 직원 목록 조회 (최대 10명)
            - getAllDepartments: 전체 부서 목록 조회
            - getOrganizationStats: 조직 전체 통계 조회 (총 직원수, 부서수, 상태별 직원수)
            - searchProjects: 프로젝트 목록 검색 (이름, 상태, 최대 20건)
            - getProjectDetail: 프로젝트 ID로 상세 조회
            - getDashboardSummary: 대시보드 요약 지표 조회
            - searchParties: 거래처 목록 검색 (이름 키워드, 최대 20건)
            - getPartyProjects: 거래처 ID로 프로젝트 목록 조회
            - getMonthlyRevenueSummary: 월별 매출 집계 조회 (yyyyMM)
            
            응답 가이드라인:
            1. 정중하고 전문적인 어조 사용
            2. 필요한 정보를 먼저 도구로 조회한 후 응답
            3. 조회된 데이터를 기반으로 정확한 정보 제공
            4. 정보가 부족할 경우 추가 질문
            5. 한국어로 응답
            6. 직원/부서/프로젝트/거래처 정보 제공 시, link 필드를 활용하여 마크다운 링크 형식으로 상세 페이지 안내
               예: "자세한 내용은 [홍길동 프로필](/employees/1)에서 확인하세요."
            7. 내부 링크는 반드시 상대경로만 사용 (/employees/{id}, /departments/{id}, /projects/{id}, /parties/{id})
               - https://... 또는 도메인이 포함된 URL은 절대 생성하지 않는다.
            
            제한 사항:
            - 제공된 도구만 사용하여 데이터 조회
            - 없는 정보는 만들지 않고 "정보를 찾을 수 없습니다" 응답
            - 사내 데이터 조회가 필요한 질문은 먼저 도구를 사용한다.
            """;

    private final ChatClient chatClient;
    private final ObjectProvider<EmployeeInfoTools> employeeInfoToolsProvider;
    private final ObjectProvider<OrganizationTools> organizationToolsProvider;
    private final ObjectProvider<BusinessQueryTools> businessQueryToolsProvider;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTitleService chatTitleService;

    public ChatCommandService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            ObjectProvider<EmployeeInfoTools> employeeInfoToolsProvider,
            ObjectProvider<OrganizationTools> organizationToolsProvider,
            ObjectProvider<BusinessQueryTools> businessQueryToolsProvider,
            ChatSessionRepository chatSessionRepository,
            ChatTitleService chatTitleService) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.employeeInfoToolsProvider = employeeInfoToolsProvider;
        this.organizationToolsProvider = organizationToolsProvider;
        this.businessQueryToolsProvider = businessQueryToolsProvider;
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
                    EmployeeInfoTools employeeInfoTools = employeeInfoToolsProvider.getObject();
                    OrganizationTools organizationTools = organizationToolsProvider.getObject();
                    BusinessQueryTools businessQueryTools = businessQueryToolsProvider.getObject();

                    // Create a sink for tool call events
                    Sinks.Many<String> toolCallSink = Sinks.many().multicast().onBackpressureBuffer();

                    // Set tool call notifier on tools
                    employeeInfoTools.setToolCallNotifier(toolCallSink::tryEmitNext);
                    organizationTools.setToolCallNotifier(toolCallSink::tryEmitNext);
                    businessQueryTools.setToolCallNotifier(toolCallSink::tryEmitNext);

                    Flux<String> contentStream = chatClient.prompt()
                            .user(command.content())
                            .tools(employeeInfoTools, organizationTools, businessQueryTools)
                            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                            .stream()
                            .content()
                            .doOnComplete(() -> {
                                // Clear notifiers and complete sink
                                employeeInfoTools.setToolCallNotifier(null);
                                organizationTools.setToolCallNotifier(null);
                                businessQueryTools.setToolCallNotifier(null);
                                toolCallSink.tryEmitComplete();
                            })
                            .doOnError(e -> {
                                employeeInfoTools.setToolCallNotifier(null);
                                organizationTools.setToolCallNotifier(null);
                                businessQueryTools.setToolCallNotifier(null);
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
        EmployeeInfoTools employeeInfoTools = employeeInfoToolsProvider.getObject();
        OrganizationTools organizationTools = organizationToolsProvider.getObject();
        BusinessQueryTools businessQueryTools = businessQueryToolsProvider.getObject();

        String response = chatClient.prompt()
                .user(command.content())
                .tools(employeeInfoTools, organizationTools, businessQueryTools)
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
