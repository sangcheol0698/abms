package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.transaction.TestTransaction;

import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("인증 API 메일 발송 실패 (AuthApi)")
@Import(AuthApiMailFailureTest.FailingRegistrationLinkSenderConfig.class)
class AuthApiMailFailureTest extends ApiIntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Test
    @DisplayName("회원가입 요청 중 메일 발송이 실패하면 500을 반환하고 토큰을 롤백한다")
    void should_rollbackRegistrationToken_whenMailSendFails() throws Exception {
        String email = "mail-failure-user@iabacus.co.kr";
        employeeRepository.save(createEmployee(email, "메일실패직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isInternalServerError());

        TestTransaction.end();
        TestTransaction.start();
        assertThat(registrationTokenRepository.findFirstByEmailOrderByCreatedAtDesc(new Email(email)))
                .isEmpty();
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

    @TestConfiguration
    static class FailingRegistrationLinkSenderConfig {

        @Bean
        @Primary
        RegistrationLinkSender failingRegistrationLinkSender() {
            return (email, token, expiresAt) -> {
                throw new MailSendException("테스트용 메일 전송 실패");
            };
        }
    }

}
