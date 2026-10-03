package kr.co.abacus.abms.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ProfitCalculationService;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 화면이 권한 범위 밖의 프로젝트 정보를 노출하지 않는지 확인한다.
 */
@IntegrationTest
class ScopeLeakWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private ProfitCalculationService calculationService;

    private Department otherTeam;
    private Employee member;
    private Project joined;
    private Project hidden;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        Department team = fixtures.department("내 팀", null);
        otherTeam = fixtures.department("다른 팀", null);
        member = fixtures.employee(team, "참여자");
        fixtures.payroll(member, 60_000_000, today.minusYears(1));
        joined = fixtures.project(otherTeam, 100_000_000, today.minusMonths(2), today.plusMonths(2));
        hidden = fixtures.project(otherTeam, 100_000_000, today.minusMonths(2), today.plusMonths(2));
        fixtures.assign(joined, member, today.minusMonths(1), today.plusMonths(1));
        calculationService.calculate(YearMonth.now());
    }

    @Test
    void 부서_화면은_프로젝트_조회_범위_밖의_프로젝트를_보여주지_않는다() throws Exception {
        LoginUser participant = Fixtures.user(member, Fixtures.grants(PermissionScope.CURRENT_PARTICIPATION, PermissionCode.PROJECT_READ));

        mvc.perform(get("/departments").param("selected", otherTeam.id().toString()).with(user(participant)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(joined.getCode())))
                .andExpect(content().string(not(containsString(hidden.getCode()))));
    }

    @Test
    void 프로젝트_손익_이력은_손익_조회_범위가_포함할_때만_보여준다() throws Exception {
        Map<PermissionCode, Set<PermissionScope>> grants = new EnumMap<>(PermissionCode.class);
        grants.put(PermissionCode.PROJECT_READ, EnumSet.of(PermissionScope.ALL));
        grants.put(PermissionCode.DASHBOARD_READ, EnumSet.of(PermissionScope.OWN_DEPARTMENT));
        LoginUser user = Fixtures.user(member, grants);

        mvc.perform(get("/projects/{id}", hidden.id()).with(user(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("월별 손익 집계"))));
        mvc.perform(get("/projects/{id}", hidden.id()).with(user(Fixtures.admin(member))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("월별 손익 집계")));
    }

}
