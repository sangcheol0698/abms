package kr.co.abacus.abms.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 명령 팔레트는 권한 범위 밖의 데이터와 권한 없는 명령을 노출하지 않는다.
 */
@IntegrationTest
class CommandPaletteWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    private Employee member;
    private Project joined;
    private Project hidden;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        Department team = fixtures.department("내 팀", null);
        Department otherTeam = fixtures.department("다른 팀", null);
        member = fixtures.employee(team, "참여자");
        joined = fixtures.project(otherTeam, 100_000_000, today.minusMonths(2), today.plusMonths(2));
        hidden = fixtures.project(otherTeam, 100_000_000, today.minusMonths(2), today.plusMonths(2));
        fixtures.assign(joined, member, today.minusMonths(1), today.plusMonths(1));
    }

    @Test
    void 프로젝트_검색은_조회_범위_안의_프로젝트만_보여준다() throws Exception {
        LoginUser participant = Fixtures.user(member, Fixtures.grants(PermissionScope.CURRENT_PARTICIPATION, PermissionCode.PROJECT_READ));

        mvc.perform(get("/palette").param("q", "P-").with(user(participant)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(joined.getCode())))
                .andExpect(content().string(not(containsString(hidden.getCode()))));
    }

    @Test
    void 권한이_없는_명령과_검색_그룹은_보여주지_않는다() throws Exception {
        LoginUser plain = Fixtures.user(member, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ));

        mvc.perform(get("/palette").with(user(plain)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("직원")))
                .andExpect(content().string(not(containsString("계정 관리"))))
                .andExpect(content().string(not(containsString("프로젝트 등록"))));
        mvc.perform(get("/palette").param("q", "협력사").with(user(plain)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/parties/"))));
    }

    @Test
    void 관리자는_검색어로_직원과_명령을_함께_찾는다() throws Exception {
        mvc.perform(get("/palette").param("q", "참여").with(user(Fixtures.admin(member))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/employees/" + member.id())));
        mvc.perform(get("/palette").param("q", "다크").with(user(Fixtures.admin(member))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-palette-action=\"theme:dark\"")));
    }

}
