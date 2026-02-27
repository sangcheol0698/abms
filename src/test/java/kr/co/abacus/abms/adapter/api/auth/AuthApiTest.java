package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.auth.RegistrationToken;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("인증 API (AuthApi)")
class AuthApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "auth-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @BeforeEach
    void setUpAccount() {
        accountRepository.save(Account.create(9_999L, USERNAME, passwordEncoder.encode(PASSWORD)));
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

    @Test
    @DisplayName("회원가입 요청 시 토큰을 생성한다")
    void should_requestRegistration() throws Exception {
        String email = "new-user@iabacus.co.kr";
        employeeRepository.save(createEmployee(email, "신규직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isOk());

        RegistrationToken registrationToken = registrationTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow();

        assertThat(registrationToken.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("회원가입 확정 시 계정을 생성한다")
    void should_confirmRegistration() throws Exception {
        String email = "confirmed-user@iabacus.co.kr";
        String newPassword = "NewPassword123!";
        employeeRepository.save(createEmployee(email, "확정직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isOk());

        RegistrationToken registrationToken = registrationTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow();

        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", registrationToken.getToken(),
                                "password", newPassword
                        ))))
                .andExpect(status().isOk());
        flushAndClear();

        Account account = accountRepository.findByUsername(new Email(email)).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, account.getPassword())).isTrue();
        assertThat(registrationTokenRepository.findByToken(registrationToken.getToken())).isEmpty();
    }

    @Test
    @DisplayName("회원가입 확정 토큰이 유효하지 않으면 400을 반환한다")
    void should_returnBadRequest_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", "invalid-token",
                                "password", "Password123!"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 요청 이메일이 회사 도메인이 아니면 400을 반환한다")
    void should_returnBadRequest_whenRegistrationEmailIsNotCompanyDomain() throws Exception {
        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "new-user@abacus.co.kr"))))
                .andExpect(status().isBadRequest());
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Employee createEmployee(String email, String name) {
        return Employee.create(
                1L,
                name,
                email,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(1995, 6, 10),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

}
