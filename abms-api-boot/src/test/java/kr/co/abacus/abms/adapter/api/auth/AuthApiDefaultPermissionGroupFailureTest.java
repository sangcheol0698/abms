package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.adapter.infrastructure.permissiongroup.PermissionGroupRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("기본 권한 그룹 미구성 시 인증 API (AuthApi)")
class AuthApiDefaultPermissionGroupFailureTest extends ApiIntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @MockitoSpyBean
    private PermissionGroupRepository permissionGroupRepository;

    @Test
    @DisplayName("기본 권한 그룹이 없으면 회원가입 확정은 500을 반환하고 계정 생성을 롤백한다")
    void should_rollbackAccountCreation_whenDefaultPermissionGroupIsMissing() throws Exception {
        String email = "default-group-failure@iabacus.co.kr";
        String password = "NewPassword123!";

        given(permissionGroupRepository.findByGroupTypeAndNameAndDeletedFalse(
                PermissionGroupType.SYSTEM,
                "일반 그룹"
        )).willReturn(Optional.empty());

        employeeRepository.save(createEmployee(email, "기본그룹미설정직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isOk());

        String token = registrationTokenRepository.findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow()
                .getToken();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", token,
                                "password", password
                        ))))
                .andExpect(status().isInternalServerError());

        TestTransaction.end();
        TestTransaction.start();

        assertThat(accountRepository.findByUsername(new Email(email))).isEmpty();
        assertThat(registrationTokenRepository.findByToken(token)).isPresent();
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
