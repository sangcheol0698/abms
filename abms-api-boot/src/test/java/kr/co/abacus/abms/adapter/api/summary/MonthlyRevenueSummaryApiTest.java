package kr.co.abacus.abms.adapter.api.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import kr.co.abacus.abms.adapter.infrastructure.summary.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("월별 매출 집계 API")
class MonthlyRevenueSummaryApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "monthly-summary-user@abacus.co.kr";
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
    private MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    private Long departmentId;

    @BeforeEach
    void setUp() {
        Department department = departmentRepository.save(Department.create(
                "SUMMARY-TEAM",
                "월별집계팀",
                DepartmentType.TEAM,
                null,
                null
        ));
        departmentId = department.getIdOrThrow();

        Employee employee = employeeRepository.save(Employee.create(
                departmentId,
                "월별집계사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));

        monthlyRevenueSummaryRepository.save(createSummary("2025-08-31", 80_000_000L, 40_000_000L));
        monthlyRevenueSummaryRepository.save(createSummary("2025-09-30", 82_000_000L, 41_000_000L));
        monthlyRevenueSummaryRepository.save(createSummary("2025-10-31", 84_000_000L, 42_000_000L));
        monthlyRevenueSummaryRepository.save(createSummary("2025-11-30", 86_000_000L, 43_000_000L));
        monthlyRevenueSummaryRepository.save(createSummary("2025-12-31", 88_000_000L, 44_000_000L));
        monthlyRevenueSummaryRepository.save(createSummary("2026-01-31", 90_000_000L, 45_000_000L));
        flushAndClear();
    }

    @Test
    @DisplayName("기준 월의 매출 집계를 조회한다")
    void getMonthlyRevenueSummary() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/monthlyRevenueSummary")
                        .param("yearMonth", "202601")
                        .session(session))
                .andDo(document("monthly-revenue-summary/get",
                        queryParameters(
                                parameterWithName("yearMonth").description("조회 기준 월(yyyyMM)")
                        ),
                        responseFields(
                                fieldWithPath("targetMonth").description("집계 대상 월의 기준일"),
                                fieldWithPath("revenue").description("매출 금액"),
                                fieldWithPath("cost").description("비용 금액"),
                                fieldWithPath("profit").description("이익 금액")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetMonth").value("2026-01-31"))
                .andExpect(jsonPath("$.revenue").value(90_000_000))
                .andExpect(jsonPath("$.cost").value(45_000_000))
                .andExpect(jsonPath("$.profit").value(45_000_000));
    }

    @Test
    @DisplayName("기준 월 포함 최근 6개월 매출 추이를 조회한다")
    void getSixMonthTrend() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/monthlyRevenueSummary/sixMonthTrend")
                        .param("yearMonth", "202601")
                        .session(session))
                .andDo(document("monthly-revenue-summary/six-month-trend",
                        queryParameters(
                                parameterWithName("yearMonth").description("조회 기준 월(yyyyMM)")
                        ),
                        responseFields(
                                fieldWithPath("[].targetMonth").description("집계 대상 월의 기준일"),
                                fieldWithPath("[].revenue").description("매출 금액"),
                                fieldWithPath("[].cost").description("비용 금액"),
                                fieldWithPath("[].profit").description("이익 금액")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetMonth").value("2025-08-31"))
                .andExpect(jsonPath("$[5].targetMonth").value("2026-01-31"));
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

    private MonthlyRevenueSummary createSummary(String targetDate, long revenue, long cost) {
        return MonthlyRevenueSummary.create(new MonthlyRevenueSummaryCreateRequest(
                1L,
                "SUMMARY-PRJ",
                "월별 집계 프로젝트",
                departmentId,
                "SUMMARY-TEAM",
                "월별집계팀",
                LocalDate.parse(targetDate),
                Money.wons(revenue),
                Money.wons(cost),
                Money.wons(revenue - cost)
        ));
    }

}
