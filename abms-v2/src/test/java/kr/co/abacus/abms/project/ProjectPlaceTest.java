package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteRepository;
import kr.co.abacus.abms.site.SiteType;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class ProjectPlaceTest {

    private static final Location CLIENT = Location.of(null, "서울 영등포구 국제금융로 10", null, new BigDecimal("37.5251"), new BigDecimal("126.9255"));
    private static final Location HQ = Location.of(null, "서울 구로구 디지털로 300", null, new BigDecimal("37.4846"), new BigDecimal("126.8972"));
    private static final Location OTHER = Location.of(null, "서울 중구 을지로 170", null, new BigDecimal("37.5664"), new BigDecimal("126.9916"));

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private ProjectPlaceService placeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MockMvc mvc;

    private Department parent;
    private Department team;
    private Site hq;
    private Project project;

    @BeforeEach
    void setUp() {
        hq = siteRepository.save(Site.create(new Site.SiteInfo("본사", SiteType.HEADQUARTERS, null, HQ, null)));
        parent = fixtures.department("상위본부", null);
        team = fixtures.department("수행팀", parent);
        parent.relocate(hq.id());
        departmentRepository.save(parent);
        project = fixtures.project(team, 100_000_000, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(3));
        Party party = partyRepository.findById(project.getPartyId()).orElseThrow();
        party.update(new Party.PartyInfo(party.getName(), null, null, null, null, null, CLIENT, null, null));
    }

    @Test
    void 고객사_상주는_주소가_없으면_협력사_주소를_쓴다() {
        project.assignWorkPlace(WorkPlace.CLIENT_SITE, null);

        ProjectPlaceService.ResolvedPlace place = placeService.resolve(project, departmentService.tree());

        assertThat(place.location().address()).isEqualTo(CLIENT.address());
        assertThat(place.source()).isEqualTo("협력사 주소");
    }

    @Test
    void 고객사_상주라도_주소를_입력하면_입력한_주소를_쓴다() {
        project.assignWorkPlace(WorkPlace.CLIENT_SITE, OTHER);

        assertThat(placeService.resolve(project, departmentService.tree()).location().address()).isEqualTo(OTHER.address());
    }

    @Test
    void 자사는_주관_부서가_속한_상위_부서의_사업장을_쓰고_입력한_주소는_버린다() {
        project.assignWorkPlace(WorkPlace.OFFICE, OTHER);

        ProjectPlaceService.ResolvedPlace place = placeService.resolve(project, departmentService.tree());

        assertThat(project.getWorkLocation().isEmpty()).isTrue();
        assertThat(place.location().address()).isEqualTo(HQ.address());
        assertThat(place.source()).contains("본사");
    }

    @Test
    void 원격과_미정은_위치가_없다() {
        project.assignWorkPlace(WorkPlace.REMOTE, OTHER);
        assertThat(placeService.resolve(project, departmentService.tree()).location().isEmpty()).isTrue();

        project.assignWorkPlace(null, null);
        assertThat(placeService.resolve(project, departmentService.tree()).label()).isEqualTo("미정");
    }

    @Test
    void 직원_근무지는_투입_중인_프로젝트의_수행_장소_없으면_부서_사업장이다() {
        Employee employee = fixtures.employee(team, "상주직원");
        ProjectAssignment assignment = fixtures.assign(project, employee, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));

        assertThat(placeService.workplaceOf(team.id(), List.of(assignment), Map.of(project.id(), project), departmentService.tree(), LocalDate.now()).name())
                .isEqualTo("본사");

        project.assignWorkPlace(WorkPlace.CLIENT_SITE, null);
        ProjectPlaceService.Workplace workplace = placeService.workplaceOf(team.id(), List.of(assignment), Map.of(project.id(), project),
                departmentService.tree(), LocalDate.now());
        assertThat(workplace.name()).isEqualTo(project.getName());
        assertThat(workplace.kind()).isEqualTo("고객사 상주");
    }

    @Test
    void 프로젝트_수정_폼으로_수행_장소를_저장한다() throws Exception {
        mvc.perform(post("/projects/{id}", project.id()).with(user(Fixtures.admin(fixtures.employee(team, "관리자")))).with(csrf())
                        .param("name", project.getName()).param("partyId", String.valueOf(project.getPartyId()))
                        .param("leadDepartmentId", String.valueOf(team.id())).param("status", project.getStatus().name())
                        .param("contractAmount", "100000000").param("startDate", project.getPeriod().startDate().toString())
                        .param("endDate", project.getPeriod().endDate().toString())
                        .param("workPlace", "OTHER").param("address", OTHER.address())
                        .param("latitude", "37.5664").param("longitude", "126.9916"))
                .andExpect(status().is3xxRedirection());

        Project saved = projectRepository.findById(project.id()).orElseThrow();
        assertThat(saved.getWorkPlace()).isEqualTo(WorkPlace.OTHER);
        assertThat(saved.getWorkLocation().address()).isEqualTo(OTHER.address());
    }

}
