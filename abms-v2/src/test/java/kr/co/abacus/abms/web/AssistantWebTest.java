package kr.co.abacus.abms.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.account.Account;
import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.assistant.ChatSession;
import kr.co.abacus.abms.assistant.ChatSessionRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class AssistantWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private LoginUser me;
    private ChatSession session;

    @BeforeEach
    void setUp() {
        Employee employee = fixtures.employee(fixtures.department("대화팀", null), "대화자");
        Account account = accountRepository.save(Account.create(employee.id(), "chat" + employee.id() + "@test.co", "{noop}x"));
        LoginUser admin = Fixtures.admin(employee);
        me = new LoginUser(account.id(), employee.id(), employee.getDepartmentId(), account.getUsername(), employee.getName(), null, "{noop}x",
                true, false, admin.grants());
        session = sessionRepository.save(ChatSession.start(me.accountId(), "이번 달 손익 알려줘"));
    }

    @Test
    void 제목은_브라우저_입력창_없이_그_자리에서_고친다() throws Exception {
        mvc.perform(get("/assistant/{id}", session.id()).with(user(me)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-inline-edit")))
                .andExpect(content().string(not(containsString("hx-prompt"))));

        mvc.perform(post("/assistant/{id}/rename", session.id()).param("title", "  10월 손익 정리  ").with(user(me)).with(csrf()))
                .andExpect(redirectedUrl("/assistant/" + session.id()));

        assertThat(sessionRepository.findById(session.id()).orElseThrow().getTitle()).isEqualTo("10월 손익 정리");
    }

    @Test
    void 빈_제목은_무시한다() throws Exception {
        mvc.perform(post("/assistant/{id}/rename", session.id()).param("title", " ").with(user(me)).with(csrf()))
                .andExpect(redirectedUrl("/assistant/" + session.id()));

        assertThat(sessionRepository.findById(session.id()).orElseThrow().getTitle()).isEqualTo("이번 달 손익 알려줘");
    }

}
