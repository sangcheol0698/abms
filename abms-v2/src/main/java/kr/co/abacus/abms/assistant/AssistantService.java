package kr.co.abacus.abms.assistant;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectRevenueService;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ProfitQueryService;

/**
 * 직원/부서/프로젝트/손익 데이터를 자연어로 조회하는 AI 어시스턴트.
 */
@Service
public class AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private static final String SYSTEM_PROMPT = """
            당신은 ABMS(프로젝트 손익 관리 시스템)의 업무 도우미입니다. 오늘 날짜는 {today} 이고, 질문자는 {name} 입니다.
            - 직원, 부서, 협력사, 프로젝트, 월별 손익 정보는 반드시 제공된 도구로 조회한 뒤 답합니다. 추측하지 마세요.
            - 도구가 권한 없음을 알리면 그대로 안내하고 우회하지 마세요.
            - 매출은 세금계산서가 발행된 청구 계획의 청구일 기준, 비용은 투입 M/M × 직원 월 원가, 손익은 프로젝트 주관 부서에 귀속됩니다.
            - 금액은 원 단위로 천 단위 구분 기호를 사용합니다.
            - 항목을 언급할 때 도구 결과의 link 를 마크다운 링크로 붙입니다. 예: [홍길동](/employees/3)
            - 한국어로 간결하게 답하고, 목록이 길면 표나 글머리표를 사용합니다.
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilder;
    private final ChatMemory chatMemory;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final AssistantProperties properties;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    private final ProjectService projectService;
    private final ProjectRevenueService revenueService;
    private final ProjectAssignmentService assignmentService;
    private final PartyService partyService;
    private final ProfitQueryService profitQueryService;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    public AssistantService(ObjectProvider<ChatClient.Builder> chatClientBuilder, ChatMemory chatMemory,
                            ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository,
                            AssistantProperties properties, EmployeeService employeeService,
                            DepartmentService departmentService, ProjectService projectService,
                            ProjectRevenueService revenueService, ProjectAssignmentService assignmentService,
                            PartyService partyService, ProfitQueryService profitQueryService,
                            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.chatClientBuilder = chatClientBuilder;
        this.chatMemory = chatMemory;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.properties = properties;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
        this.projectService = projectService;
        this.revenueService = revenueService;
        this.assignmentService = assignmentService;
        this.partyService = partyService;
        this.profitQueryService = profitQueryService;
        this.transactionTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> sessions(LoginUser user) {
        return sessionRepository.findAllByAccountIdOrderByFavoriteDescLastMessageAtDesc(user.accountId());
    }

    @Transactional(readOnly = true)
    public ChatSession session(LoginUser user, Long sessionId) {
        return ownedSession(user, sessionId);
    }

    /** 본인 대화만 찾는다. 트랜잭션 없이 부르는 ask() 와 공유하므로 프록시를 거치지 않는 private 메서드로 둔다. */
    private ChatSession ownedSession(LoginUser user, Long sessionId) {
        return sessionRepository.findById(sessionId)
                .filter(s -> s.getAccountId().equals(user.accountId()))
                .orElseThrow(() -> NotFoundException.of("대화", sessionId));
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> messages(LoginUser user, Long sessionId) {
        ownedSession(user, sessionId);
        return messageRepository.findAllBySessionIdOrderByIdAsc(sessionId);
    }

    @Transactional
    public ChatSession startSession(LoginUser user, String firstMessage) {
        validate(firstMessage);
        return sessionRepository.save(ChatSession.start(user.accountId(), firstMessage));
    }

    /**
     * 사용자 메시지를 저장하고 LLM 응답을 받아 저장한다.
     * LLM 호출은 트랜잭션 밖에서 수행해 DB 커넥션을 오래 점유하지 않는다.
     */
    public Exchange ask(LoginUser user, Long sessionId, String message) {
        validate(message);
        ChatSession session = ownedSession(user, sessionId);
        ChatMessage question = transactionTemplate.execute(status -> {
            sessionRepository.findById(sessionId).ifPresent(ChatSession::touch);
            return messageRepository.save(ChatMessage.of(sessionId, ChatMessage.Role.USER, message.strip()));
        });

        String answer;
        if (!properties.isConfigured()) {
            answer = "AI 어시스턴트가 아직 설정되지 않았습니다. 관리자에게 `OPENAI_API_KEY` 환경 변수 설정을 요청하세요.";
        } else {
            try {
                answer = call(user, session.getConversationId(), message.strip());
            } catch (RuntimeException e) {
                log.warn("AI 어시스턴트 호출 실패: {}", e.getMessage(), e);
                answer = "죄송합니다. 응답을 생성하는 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
            }
        }
        String finalAnswer = answer == null || answer.isBlank() ? "(응답 없음)" : answer;
        ChatMessage reply = transactionTemplate.execute(status ->
                messageRepository.save(ChatMessage.of(sessionId, ChatMessage.Role.ASSISTANT, finalAnswer)));
        return new Exchange(question, reply);
    }

    @Transactional
    public void rename(LoginUser user, Long sessionId, String title) {
        ownedSession(user, sessionId).rename(title);
    }

    @Transactional
    public void toggleFavorite(LoginUser user, Long sessionId) {
        ownedSession(user, sessionId).toggleFavorite();
    }

    @Transactional
    public void delete(LoginUser user, Long sessionId) {
        ChatSession session = ownedSession(user, sessionId);
        chatMemory.clear(session.getConversationId());
        session.softDelete(user.accountId());
    }

    private String call(LoginUser user, String conversationId, String message) {
        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("ChatClient 를 사용할 수 없습니다.");
        }
        AssistantTools tools = new AssistantTools(user, employeeService, departmentService, projectService,
                revenueService, assignmentService, partyService, profitQueryService);
        return builder.build().prompt()
                .system(s -> s.text(SYSTEM_PROMPT).param("today", LocalDate.now().toString()).param("name", user.name()))
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(tools)
                .call()
                .content();
    }

    private static void validate(String message) {
        if (message == null || message.isBlank()) {
            throw new BusinessException("메시지를 입력하세요.");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException("메시지는 " + MAX_MESSAGE_LENGTH + "자 이하로 입력하세요.");
        }
    }

    public record Exchange(ChatMessage question, ChatMessage answer) {
    }

}
