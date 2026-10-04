package kr.co.abacus.abms.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class EmployeeWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department team;
    private Employee me;
    private LoginUser admin;

    @BeforeEach
    void setUp() {
        team = fixtures.department("개발팀", null);
        me = fixtures.employee(team, "관리자");
        admin = Fixtures.admin(me);
    }

    @Test
    void 직원_목록을_렌더링한다() throws Exception {
        mvc.perform(get("/employees").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<html")))
                .andExpect(content().string(containsString("관리자")));
    }

    @Test
    void HTMX_검색은_결과_영역만_응답한다() throws Exception {
        fixtures.employee(team, "검색대상");
        mvc.perform(get("/employees").param("q", "검색").with(user(admin))
                        .header("HX-Request", "true").header("HX-Target", "employee-results"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<html"))))
                .andExpect(content().string(containsString("검색대상")))
                .andExpect(content().string(not(containsString(">관리자<"))));
    }

    @Test
    void 직원을_등록하면_상세_화면으로_이동한다() throws Exception {
        mvc.perform(post("/employees").with(user(admin)).with(csrf())
                        .param("departmentId", team.id().toString())
                        .param("name", "신규직원")
                        .param("email", "new.hire@test.co")
                        .param("joinDate", "2026-03-02")
                        .param("birthDate", "1999-05-05")
                        .param("position", "ASSOCIATE")
                        .param("type", "FULL_TIME")
                        .param("grade", "JUNIOR")
                        .param("annualSalary", "48000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/employees/*"))
                .andExpect(flash().attributeExists("toast"));

        assertThat(employeeRepository.existsByEmailAndDeletedFalse("new.hire@test.co")).isTrue();
    }

    @Test
    void 검증_오류는_422와_함께_폼을_다시_보여준다() throws Exception {
        mvc.perform(post("/employees").with(user(admin)).with(csrf())
                        .param("departmentId", team.id().toString())
                        .param("email", "invalid")
                        .param("position", "ASSOCIATE").param("type", "FULL_TIME").param("grade", "JUNIOR"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("이름을 입력하세요.")))
                .andExpect(content().string(containsString("이메일 형식이 올바르지 않습니다.")))
                .andExpect(content().string(containsString("입사일을 입력하세요.")));
    }

    @Test
    void 쓰기_권한이_없으면_등록_화면에_접근할_수_없다() throws Exception {
        LoginUser reader = Fixtures.user(me, Fixtures.grants(PermissionScope.ALL, PermissionCode.EMPLOYEE_READ));
        mvc.perform(get("/employees/new").with(user(reader))).andExpect(status().isForbidden());
    }

    @Test
    void 모달_폼_처리_후_HX_Redirect_로_상세로_이동한다() throws Exception {
        Employee target = fixtures.employee(team, "퇴사예정");
        mvc.perform(post("/employees/{id}/resign", target.id()).with(user(admin)).with(csrf())
                        .header("HX-Request", "true").param("resignationDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/employees/" + target.id()));

        assertThat(target.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
    }

    @Test
    void 업무_규칙_위반은_HTMX_토스트로_알린다() throws Exception {
        Employee target = fixtures.employee(team, "휴직자");
        target.takeLeave();
        mvc.perform(post("/employees/{id}/leave", target.id()).with(user(admin)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("HX-Reswap", "none"))
                .andExpect(header().string("HX-Trigger", containsString("\"type\":\"error\"")));
    }

    @Test
    void CSV_내보내기는_BOM_을_포함한_UTF8_파일이다() throws Exception {
        byte[] body = mvc.perform(get("/employees/export").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();
        String csv = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).startsWith("﻿이름,이메일").contains("관리자");
    }

}
