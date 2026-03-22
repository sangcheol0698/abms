package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.adapter.infrastructure.summary.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("인증 전용 API 경계")
class AuthenticatedApiBoundaryTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "authenticated-only-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    @Autowired
    private MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    private Long employeeId;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "AUTH-ONLY-TEAM",
                "인증전용팀",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "인증전용사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        employeeId = employee.getIdOrThrow();

        accountRepository.save(Account.create(
                employeeId,
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));

        positionHistoryRepository.save(PositionHistory.create(new PositionHistoryCreateRequest(
                employeeId,
                new Period(LocalDate.of(2024, 1, 1), null),
                EmployeePosition.ASSOCIATE
        )));

        monthlyRevenueSummaryRepository.save(MonthlyRevenueSummary.create(new MonthlyRevenueSummaryCreateRequest(
                1L,
                "AUTH-ONLY-PRJ",
                "인증 전용 프로젝트",
                department.getIdOrThrow(),
                department.getCode(),
                department.getName(),
                LocalDate.of(2026, 1, 31),
                Money.wons(100_000_000L),
                Money.wons(60_000_000L),
                Money.wons(40_000_000L)
        )));
        flushAndClear();
    }

    @Test
    @DisplayName("인증 전용 API는 로그인하지 않으면 401을 반환한다")
    void should_requireAuthentication_forAuthenticatedOnlyApis() throws Exception {
        mockMvc.perform(get("/api/dashboards/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/monthlyRevenueSummary").param("yearMonth", "202601"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/positionHistory/{employeeId}", employeeId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/employees/excel/sample"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/projects/excel/sample"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/chat/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("권한이 없어도 인증만 되면 인증 전용 API에 접근할 수 있다")
    void should_allowAuthenticatedUser_forAuthenticatedOnlyApis() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/dashboards/summary").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployeesCount").isNumber());

        mockMvc.perform(get("/api/monthlyRevenueSummary")
                        .param("yearMonth", "202601")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetMonth").value("2026-01-31"))
                .andExpect(jsonPath("$.revenue").value(100000000))
                .andExpect(jsonPath("$.cost").value(60000000))
                .andExpect(jsonPath("$.profit").value(40000000));

        mockMvc.perform(get("/api/positionHistory/{employeeId}", employeeId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value("ASSOCIATE"));

        mockMvc.perform(get("/api/employees/excel/sample").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        mockMvc.perform(get("/api/projects/excel/sample").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        mockMvc.perform(get("/api/notifications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/v1/chat/sessions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
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
