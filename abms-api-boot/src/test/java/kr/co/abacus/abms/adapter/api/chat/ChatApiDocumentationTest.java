package kr.co.abacus.abms.adapter.api.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.chat.ChatCommandService;
import kr.co.abacus.abms.application.chat.ChatQueryService;
import kr.co.abacus.abms.application.chat.dto.query.ChatMessageDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionDetail;
import kr.co.abacus.abms.application.chat.dto.query.ChatSessionSummary;
import kr.co.abacus.abms.application.chat.dto.query.ChatStreamResult;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("채팅 API 문서화")
class ChatApiDocumentationTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "chat-docs-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ChatCommandService chatCommandService;

    @MockitoBean
    private ChatQueryService chatQueryService;

    private final LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 9, 0);
    private final LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 9, 5);

    @BeforeEach
    void setUp() {
        Employee employee = employeeRepository.save(Employee.create(
                1L,
                "채팅문서사용자",
                USERNAME,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        accountRepository.save(Account.create(employee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));
        flushAndClear();
    }

    @Test
    @DisplayName("채팅 스트림 엔드포인트를 문서화한다")
    void streamChat() throws Exception {
        given(chatCommandService.streamMessage(anyLong(), any()))
                .willReturn(Mono.just(new ChatStreamResult(
                        "session-001",
                        Flux.just("안녕하세요."),
                        Flux.just("searchEmployees")
                )));

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(post("/api/v1/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "직원 정보를 찾아줘",
                                "sessionId", "session-001"
                        )))
                        .session(session))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andDo(document("chat/stream",
                        requestFields(
                                fieldWithPath("content").description("사용자 질문 또는 지시문"),
                                fieldWithPath("sessionId").description("기존 채팅 세션 ID").optional()
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("일반 채팅 메시지 전송 엔드포인트를 문서화한다")
    void sendMessage() throws Exception {
        given(chatCommandService.sendMessage(anyLong(), any())).willReturn("직원 정보를 찾았습니다.");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/v1/chat/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "직원 정보를 찾아줘",
                                "sessionId", "session-001"
                        )))
                        .session(session))
                .andDo(document("chat/message",
                        requestFields(
                                fieldWithPath("content").description("사용자 질문 또는 지시문"),
                                fieldWithPath("sessionId").description("기존 채팅 세션 ID").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").description("응답 메시지 ID").optional(),
                                fieldWithPath("role").description("메시지 작성자 역할"),
                                fieldWithPath("content").description("어시스턴트 응답 내용"),
                                fieldWithPath("timestamp").description("응답 생성 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ASSISTANT"));
    }

    @Test
    @DisplayName("최근 채팅 세션 목록을 문서화한다")
    void getRecentSessions() throws Exception {
        given(chatQueryService.getRecentSessions(anyLong(), anyInt()))
                .willReturn(List.of(new ChatSessionSummary(
                        1L,
                        "session-001",
                        "직원 조회",
                        false,
                        updatedAt
                )));
        MockHttpSession session = login();

        mockMvc.perform(get("/api/v1/chat/sessions")
                        .param("limit", "20")
                        .session(session))
                .andDo(document("chat/sessions",
                        queryParameters(
                                parameterWithName("limit").description("조회할 최근 세션 수").optional()
                        ),
                        responseFields(
                                fieldWithPath("[].id").description("내부 채팅 세션 식별자"),
                                fieldWithPath("[].sessionId").description("외부 노출용 채팅 세션 ID"),
                                fieldWithPath("[].title").description("채팅 세션 제목"),
                                fieldWithPath("[].favorite").description("즐겨찾기 여부"),
                                fieldWithPath("[].messages").description("메시지 목록"),
                                fieldWithPath("[].createdAt").description("세션 생성 시각"),
                                fieldWithPath("[].updatedAt").description("세션 최종 수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value("session-001"));
    }

    @Test
    @DisplayName("즐겨찾기 채팅 세션 목록을 문서화한다")
    void getFavoriteSessions() throws Exception {
        given(chatQueryService.getFavoriteSessions(anyLong()))
                .willReturn(List.of(new ChatSessionSummary(
                        1L,
                        "session-001",
                        "즐겨찾기 세션",
                        true,
                        updatedAt
                )));
        MockHttpSession session = login();

        mockMvc.perform(get("/api/v1/chat/sessions/favorites").session(session))
                .andDo(document("chat/favorites",
                        responseFields(
                                fieldWithPath("[].id").description("내부 채팅 세션 식별자"),
                                fieldWithPath("[].sessionId").description("외부 노출용 채팅 세션 ID"),
                                fieldWithPath("[].title").description("채팅 세션 제목"),
                                fieldWithPath("[].favorite").description("즐겨찾기 여부"),
                                fieldWithPath("[].messages").description("메시지 목록"),
                                fieldWithPath("[].createdAt").description("세션 생성 시각"),
                                fieldWithPath("[].updatedAt").description("세션 최종 수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].favorite").value(true));
    }

    @Test
    @DisplayName("채팅 세션 상세 조회를 문서화한다")
    void getSessionDetail() throws Exception {
        given(chatQueryService.getSessionDetail(anyLong(), any()))
                .willReturn(new ChatSessionDetail(
                        1L,
                        "session-001",
                        "직원 조회",
                        true,
                        List.of(
                                new ChatMessageDetail(1L, "USER", "직원 정보를 찾아줘", createdAt),
                                new ChatMessageDetail(2L, "ASSISTANT", "직원 정보를 찾았습니다.", updatedAt)
                        ),
                        createdAt,
                        updatedAt
                ));
        MockHttpSession session = login();

        mockMvc.perform(get("/api/v1/chat/sessions/{sessionId}", "session-001").session(session))
                .andDo(document("chat/detail",
                        pathParameters(
                                parameterWithName("sessionId").description("조회할 채팅 세션 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("내부 채팅 세션 식별자"),
                                fieldWithPath("sessionId").description("외부 노출용 채팅 세션 ID"),
                                fieldWithPath("title").description("채팅 세션 제목"),
                                fieldWithPath("favorite").description("즐겨찾기 여부"),
                                fieldWithPath("messages").description("메시지 목록"),
                                fieldWithPath("messages[].id").description("메시지 식별자").optional(),
                                fieldWithPath("messages[].role").description("메시지 작성자 역할"),
                                fieldWithPath("messages[].content").description("메시지 본문"),
                                fieldWithPath("messages[].timestamp").description("메시지 생성 시각").optional(),
                                fieldWithPath("createdAt").description("세션 생성 시각"),
                                fieldWithPath("updatedAt").description("세션 최종 수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].role").value("USER"));
    }

    @Test
    @DisplayName("채팅 세션 즐겨찾기 토글을 문서화한다")
    void toggleFavorite() throws Exception {
        willDoNothing().given(chatCommandService).toggleFavorite(anyLong(), any());
        MockHttpSession session = login();

        mockMvc.perform(post("/api/v1/chat/sessions/{sessionId}/favorite", "session-001").session(session))
                .andDo(document("chat/toggle-favorite",
                        pathParameters(
                                parameterWithName("sessionId").description("즐겨찾기 토글할 채팅 세션 ID")
                        )))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채팅 세션 제목 수정 엔드포인트를 문서화한다")
    void updateSessionTitle() throws Exception {
        willDoNothing().given(chatCommandService).updateSessionTitle(anyLong(), any(), any());
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/v1/chat/sessions/{sessionId}/title", "session-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "새 제목")))
                        .session(session))
                .andDo(document("chat/update-title",
                        pathParameters(
                                parameterWithName("sessionId").description("제목을 수정할 채팅 세션 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").description("새 채팅 세션 제목")
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("채팅 세션 삭제 엔드포인트를 문서화한다")
    void deleteSession() throws Exception {
        willDoNothing().given(chatCommandService).deleteSession(anyLong(), any());
        MockHttpSession session = login();

        mockMvc.perform(delete("/api/v1/chat/sessions/{sessionId}", "session-001").session(session))
                .andDo(document("chat/delete-session",
                        pathParameters(
                                parameterWithName("sessionId").description("삭제할 채팅 세션 ID")
                        )))
                .andExpect(status().isNoContent());
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

}
