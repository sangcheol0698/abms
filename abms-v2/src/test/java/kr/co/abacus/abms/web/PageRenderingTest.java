package kr.co.abacus.abms.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @ValueSource(strings = {"/", "/employees", "/employees/new", "/departments", "/parties", "/parties/new", "/projects",
            "/projects/new", "/summary", "/summary?month=2026-01", "/reports", "/assistant", "/admin/accounts",
            "/admin/permission-groups", "/admin/permission-groups/1", "/admin/cost-policies", "/notifications"})
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
    void 없는_페이지는_404_오류_화면() throws Exception {
        mvc.perform(get("/projects/{id}", 9_999_999).with(user(admin))).andExpect(status().isNotFound());
    }

}
