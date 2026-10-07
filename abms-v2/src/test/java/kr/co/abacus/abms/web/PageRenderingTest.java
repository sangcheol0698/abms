package kr.co.abacus.abms.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 주요 화면이 오류 없이 렌더링되는지 확인한다. (JTE 템플릿 + 모델 바인딩 스모크 테스트)
 */
@IntegrationTest
class PageRenderingTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    private LoginUser admin;
    private Project project;
    private Employee employee;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("팀", null);
        employee = fixtures.employee(team, "직원");
        fixtures.payroll(employee, 60_000_000, LocalDate.of(2025, 1, 1));
        project = fixtures.project(team, 300_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        fixtures.revenue(project, 1, LocalDate.of(2026, 1, 31), 100_000_000, true);
        fixtures.assign(project, employee, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        admin = Fixtures.admin(employee);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/employees", "/employees/new", "/departments", "/departments/new", "/parties", "/parties/new", "/sites", "/sites/new", "/projects",
            "/projects/new", "/summary", "/summary?month=2026-01", "/summary?month=2026-01&basis=managed", "/reports", "/assistant", "/admin/accounts",
            "/admin/permission-groups", "/admin/permission-groups/1", "/admin/cost-policies", "/admin/audit-logs", "/notices", "/notices/new", "/notifications"})
    void 목록과_폼_화면을_렌더링한다(String path) throws Exception {
        mvc.perform(get(path).with(user(admin))).andExpect(status().isOk());
    }

    @Test
    void 상세_화면과_모달을_렌더링한다() throws Exception {
        mvc.perform(get("/employees/{id}", employee.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/employees/{id}/edit", employee.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/employees/{id}/promote", employee.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/{id}", project.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/{id}/edit", project.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/{id}/revenues/new", project.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/{id}/assignments/new", project.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/projects/{id}/expenses/new", project.id()).with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/departments/{id}", employee.getDepartmentId()).with(user(admin)).header("HX-Request", "true")
                .header("HX-Target", "department-detail")).andExpect(status().isOk());
    }

    @Test
    void 모달에서_매출_계획을_추가하면_섹션을_다시_그린다() throws Exception {
        mvc.perform(post("/projects/{id}/revenues", project.id()).with(user(admin)).with(csrf()).header("HX-Request", "true")
                        .param("sequence", "2").param("revenueDate", "2026-03-31").param("type", "BALANCE_PAYMENT").param("amount", "50000000"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Retarget", "#revenue-section"))
                .andExpect(header().string("HX-Trigger", org.hamcrest.Matchers.containsString("closeModal")));
    }

    @Test
    void 모달에서_직접비를_추가하면_섹션을_다시_그린다() throws Exception {
        mvc.perform(post("/projects/{id}/expenses", project.id()).with(user(admin)).with(csrf()).header("HX-Request", "true")
                        .param("expenseDate", "2026-03-15").param("category", "LICENSE").param("amount", "1200000")
                        .param("description", "IDE 라이선스"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Retarget", "#expense-section"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("IDE 라이선스")));
        // 상세 화면에도 직접비 섹션이 보인다.
        mvc.perform(get("/projects/{id}", project.id()).with(user(admin)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1,200,000")));
    }

    @Test
    void 연도_재집계_후_보던_월과_매출_기준으로_돌아온다() throws Exception {
        mvc.perform(post("/summary/recalculate-year").param("year", "2025").param("month", "2025-06").param("basis", "managed")
                        .with(user(admin)).with(csrf()))
                .andExpect(header().string("Location", "/summary?month=2025-06&basis=managed"));
        mvc.perform(get("/summary").param("month", "2025-06").with(user(admin)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2025년 전체 재집계")));
    }

    @Test
    void 재집계와_마감_해제_후에도_보던_매출_기준을_유지한다() throws Exception {
        mvc.perform(post("/summary/recalculate").param("month", "2026-01").param("basis", "managed").with(user(admin)).with(csrf()))
                .andExpect(header().string("Location", "/summary?month=2026-01&basis=managed"));
        mvc.perform(post("/summary/recalculate").param("month", "2026-01").with(user(admin)).with(csrf()))
                .andExpect(header().string("Location", "/summary?month=2026-01"));
        mvc.perform(get("/summary").param("month", "2026-01").param("basis", "managed").with(user(admin)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<input type=\"hidden\" name=\"month\" value=\"2026-01\"><input type=\"hidden\" name=\"basis\" value=\"managed\">")));
    }

    @Test
    void 없는_페이지는_404_오류_화면() throws Exception {
        mvc.perform(get("/projects/{id}", 9_999_999).with(user(admin))).andExpect(status().isNotFound());
    }

}
