package kr.co.abacus.abms.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.party.PartyType;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteRepository;
import kr.co.abacus.abms.site.SiteType;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 사업장 등록·조회와 부서 연결, 협력사 위치 저장. (테스트 환경에는 지도 키가 없어 대체 화면으로 렌더링된다)
 */
@IntegrationTest
class SiteWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PartyRepository partyRepository;

    private Department team;
    private LoginUser admin;
    private LoginUser reader;

    @BeforeEach
    void setUp() {
        team = fixtures.department("위치팀", null);
        Employee employee = fixtures.employee(team, "위치담당");
        admin = Fixtures.admin(employee);
        reader = Fixtures.user(employee, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ));
    }

    @Test
    void 주소와_좌표를_입력해_사업장을_등록한다() throws Exception {
        mvc.perform(post("/sites").with(user(admin)).with(csrf())
                        .param("name", "판교 연구소").param("siteType", "RESEARCH")
                        .param("zipCode", "13494").param("address", "경기 성남시 분당구 판교역로 235").param("addressDetail", "5층")
                        .param("latitude", "37.4020000").param("longitude", "127.1086000"))
                .andExpect(status().is3xxRedirection());

        Site site = siteRepository.findAll().stream().filter(s -> s.getName().equals("판교 연구소")).findFirst().orElseThrow();
        assertThat(site.getSiteType()).isEqualTo(SiteType.RESEARCH);
        assertThat(site.getLocation().fullAddress()).isEqualTo("경기 성남시 분당구 판교역로 235 5층");
        assertThat(site.getLocation().latitude()).isEqualByComparingTo("37.402");
    }

    @Test
    void 주소_없이도_사업장을_등록한다() throws Exception {
        mvc.perform(post("/sites").with(user(admin)).with(csrf()).param("name", "재택").param("siteType", "ETC"))
                .andExpect(status().is3xxRedirection());

        Site site = siteRepository.findAll().stream().filter(s -> s.getName().equals("재택")).findFirst().orElseThrow();
        assertThat(site.getLocation().isEmpty()).isTrue();
        mvc.perform(get("/sites/{id}", site.id()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("주소가 등록되지 않았습니다.")));
    }

    @Test
    void 상세_화면은_연결된_부서와_가까운_협력사를_보여준다() throws Exception {
        Site site = siteRepository.save(Site.create(new Site.SiteInfo("본사", SiteType.HEADQUARTERS, null,
                Location.of(null, "서울 중구 세종대로 110", null, new BigDecimal("37.5662952"), new BigDecimal("126.9779451")), null)));
        team.relocate(site.id());
        departmentRepository.save(team);
        partyRepository.save(Party.create(new Party.PartyInfo("근처상사", null, PartyType.CLIENT, null, null, null,
                Location.of(null, "서울 중구 을지로 1", null, new BigDecimal("37.5660000"), new BigDecimal("126.9800000")), null, null)));

        mvc.perform(get("/sites/{id}", site.id()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("위치팀")))
                .andExpect(content().string(containsString("근처상사")))
                .andExpect(content().string(containsString("https://map.kakao.com/link/map/")));
        mvc.perform(get("/departments").param("selected", String.valueOf(team.id())).with(user(admin)))
                .andExpect(content().string(containsString("/sites/" + site.id())));
    }

    @Test
    void 부서가_연결된_사업장은_삭제할_수_없다() throws Exception {
        Site site = siteRepository.save(Site.create(new Site.SiteInfo("지사", SiteType.BRANCH, null, null, null)));
        team.relocate(site.id());
        departmentRepository.save(team);

        mvc.perform(post("/sites/{id}/delete", site.id()).with(user(admin)).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(siteRepository.findById(site.id())).isPresent();
    }

    @Test
    void 부서_수정에서_사업장을_지정한다() throws Exception {
        Site site = siteRepository.save(Site.create(new Site.SiteInfo("강남 사무소", SiteType.OFFICE, null, null, null)));

        mvc.perform(post("/departments/{id}", team.id()).with(user(admin)).with(csrf()).header("HX-Request", "true")
                        .param("name", "위치팀").param("type", "TEAM").param("siteId", String.valueOf(site.id())))
                .andExpect(status().isOk());

        assertThat(departmentRepository.findById(team.id()).orElseThrow().getSiteId()).isEqualTo(site.id());
    }

    @Test
    void 부서_관리_권한이_없으면_조회만_할_수_있다() throws Exception {
        mvc.perform(get("/sites").with(user(reader)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("사업장 등록"))));
        mvc.perform(get("/sites/new").with(user(reader))).andExpect(status().isForbidden());
        mvc.perform(post("/sites").with(user(reader)).with(csrf()).param("name", "몰래")).andExpect(status().isForbidden());
    }

    @Test
    void 협력사의_주소와_좌표를_저장하고_상세에_위치를_보여준다() throws Exception {
        mvc.perform(post("/parties").with(user(admin)).with(csrf())
                        .param("name", "지도상사").param("partyType", "CLIENT")
                        .param("address", "서울 영등포구 국제금융로 10").param("latitude", "37.5251000").param("longitude", "126.9255000"))
                .andExpect(status().is3xxRedirection());

        Party party = partyRepository.findAll().stream().filter(p -> p.getName().equals("지도상사")).findFirst().orElseThrow();
        assertThat(party.getLocation().hasCoordinates()).isTrue();
        mvc.perform(get("/parties/{id}", party.id()).with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("서울 영등포구 국제금융로 10")))
                .andExpect(content().string(containsString("KAKAO_JS_KEY")));
    }

    @Test
    void 목록은_사업장별_부서_수와_인원을_집계한다() throws Exception {
        Site site = siteRepository.save(Site.create(new Site.SiteInfo("집계 사옥", SiteType.BRANCH, null, null, null)));
        Department other = fixtures.department("집계팀", null);
        fixtures.employee(other, "집계1");
        fixtures.employee(other, "집계2");
        team.relocate(site.id());
        other.relocate(site.id());
        departmentRepository.save(team);
        departmentRepository.save(other);

        mvc.perform(get("/sites").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("부서 2곳")))
                .andExpect(content().string(containsString("3명")));
    }

    @Test
    void 협력사_목록은_협력사별_프로젝트_수를_보여준다() throws Exception {
        fixtures.project(team, 100_000_000, java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 12, 31));

        mvc.perform(get("/parties").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("프로젝트 1건")));
    }

}
