package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("인증 API (AuthApi)")
class AuthApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "auth-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpAccount() {
        accountRepository.save(Account.create(1L, USERNAME, passwordEncoder.encode(PASSWORD)));
        flushAndClear();
    }

    @Test
    @DisplayName("로그인하지 않으면 보호된 API에 접근할 수 없다")
    void should_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/employees/positions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인에 성공하면 세션으로 보호된 API에 접근할 수 있다")
    void should_loginAndAccessProtectedApi() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void should_failLogin_whenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", "WrongPassword!"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이메일 형식이 아니면 400을 반환한다")
    void should_returnBadRequest_whenUsernameIsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", "invalid-email",
                                "password", PASSWORD
                        ))))
                .andExpect(status().isBadRequest());
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

}
