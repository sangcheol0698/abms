package kr.co.abacus.abms.common.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class AuditLogTest {

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AuditQueryService auditQueryService;

    private Department team;
    private Employee actor;

    @BeforeEach
    void setUp() {
        team = fixtures.department("이력팀", null);
        actor = fixtures.employee(team, "변경자");
        LoginUser user = Fixtures.admin(actor);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 수정하면_바뀐_속성의_이전값과_이후값을_변경자와_함께_남긴다() {
        Project project = fixtures.project(team, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        entityManager.flush();

        Department other = fixtures.department("새 주관", null);
        project.update(new Project.ProjectInfo(project.getPartyId(), other.id(), project.getName(), null, project.getStatus(),
                Money.wons(150_000_000), new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))));
        entityManager.flush();

        AuditEntry update = auditQueryService.history("Project", project.id(), 10).getFirst();
        assertThat(update.action()).isEqualTo("UPDATE");
        assertThat(update.actor()).isEqualTo("변경자");
        assertThat(update.changes()).extracting(AuditEntry.Change::label, AuditEntry.Change::before, AuditEntry.Change::after)
                .contains(org.assertj.core.groups.Tuple.tuple("계약금액", "100000000", "150000000"),
                        org.assertj.core.groups.Tuple.tuple("주관 부서", "이력팀", "새 주관"));
    }

    @Test
    void 하위_엔티티_이력은_상위_엔티티_이력에_함께_보인다() {
        Project project = fixtures.project(team, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        ProjectRevenuePlan plan = fixtures.revenue(project, 1, LocalDate.of(2026, 3, 31), 30_000_000, false);
        entityManager.flush();

        List<AuditEntry> history = auditQueryService.history("Project", project.id(), 10);

        assertThat(history).anySatisfy(e -> {
            assertThat(e.entityLabel()).isEqualTo("매출 계획");
            assertThat(e.entityId()).isEqualTo(plan.id());
            assertThat(e.action()).isEqualTo("CREATE");
        });
    }

    @Test
    void 소프트_삭제는_삭제로_기록하고_감사_컬럼만_바뀐_수정은_남기지_않는다() {
        Party party = fixtures.party("이력상사");
        entityManager.flush();
        party.softDelete(1L);
        entityManager.flush();

        List<AuditEntry> history = auditQueryService.history("Party", party.id(), 10);

        assertThat(history).extracting(AuditEntry::action).containsExactly("DELETE", "CREATE");
    }

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mvc;

    @Test
    void 상세_화면과_관리_화면에서_이력을_보여주고_관리_권한이_없으면_막는다() throws Exception {
        Party party = fixtures.party("화면상사");
        entityManager.flush();
        LoginUser plain = Fixtures.user(actor, Fixtures.grants(kr.co.abacus.abms.access.PermissionScope.ALL,
                kr.co.abacus.abms.access.PermissionCode.PARTY_READ));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/parties/{id}", party.id())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(plain)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("변경 이력")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/audit-logs")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(Fixtures.admin(actor))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("화면상사")));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/audit-logs")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(plain)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

}
