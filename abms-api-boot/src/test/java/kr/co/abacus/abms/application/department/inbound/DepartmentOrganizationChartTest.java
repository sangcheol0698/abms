package kr.co.abacus.abms.application.department.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("조직도 조회 (DepartmentOrganizationChart)")
class DepartmentOrganizationChartTest extends IntegrationTestBase {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentManager departmentManager;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("전체 부서의 조직도 트리 구조를 조회한다")
    void getOrganizationChart_returnsDefaultTree() {
        // given
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));
        departmentFinder.clearOrganizationChartCache();

        // when
        List<OrganizationChartDetail> charts = departmentFinder.getOrganizationChart();

        // then: root
        OrganizationChartDetail chart = charts.get(0);

        assertThat(chart).isNotNull();
        assertThat(chart.departmentId()).isEqualTo(company.getId());
        assertThat(chart.departmentName()).isEqualTo("ABC Corp");
        assertThat(chart.departmentCode()).isEqualTo("COMP001");
        assertThat(chart.departmentType()).isEqualTo(DepartmentType.COMPANY);
        assertThat(chart.leader()).isNull();

        // and: division level
        assertThat(chart.children()).hasSize(1);
        OrganizationChartDetail div = chart.children().getFirst();
        assertThat(div.departmentName()).isEqualTo("ABC Corp");
        assertThat(div.departmentCode()).isEqualTo("DIV001");
        assertThat(div.departmentType()).isEqualTo(DepartmentType.DIVISION);
        assertThat(div.leader()).isNull();

        // and: team level
        assertThat(div.children()).hasSize(2);
        OrganizationChartDetail team = div.children().getFirst();
        assertThat(team.departmentName()).isEqualTo("ABC Corp");
        assertThat(team.departmentCode()).isEqualTo("TEAM001");
        assertThat(team.departmentType()).isEqualTo(DepartmentType.TEAM);
        assertThat(team.leader()).isNull();
    }

    @Test
    @DisplayName("조직도 조회 시 캐싱 확인")
    void getOrganizationChart_testCache() {
        departmentFinder.clearOrganizationChartCache();

        Cache cache = cacheManager.getCache("organizationChart");
        assertThat(cache.get(SimpleKey.EMPTY)).isNull();

        departmentFinder.getOrganizationChart();

        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();
    }

    @Test
    @DisplayName("조직도 조회 성능 테스트 - 캐싱 확인")
    void getOrganizationChart_testPerformance() {
        departmentFinder.clearOrganizationChartCache();

        long start1 = System.currentTimeMillis();
        departmentFinder.getOrganizationChart();
        long duration1 = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        departmentFinder.getOrganizationChart();
        long duration2 = System.currentTimeMillis() - start2;

        // 두 번째 호출이 첫 번째보다 현저히 빨라야 함
        assertThat(duration2).isLessThan(duration1);
    }

    @Test
    @DisplayName("조직도 조회 시 부서별 소속 직원 수를 포함한다")
    void getOrganizationChard_employeeCount() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.save(company);
        departmentFinder.clearOrganizationChartCache();

        employeeRepository.save(createEmployee(company.getId(), "test1@email.com"));
        employeeRepository.save(createEmployee(company.getId(), "test2@email.com"));
        employeeRepository.save(createEmployee(company.getId(), "test3@email.com"));

        List<OrganizationChartDetail> charts = departmentFinder.getOrganizationChart();

        // then: root
        OrganizationChartDetail chart = charts.get(0);
        int employeeCount = chart.employeeCount();

        // then: 3 employees in each team
        assertThat(employeeCount).isEqualTo(3);
    }

    @Test
    @DisplayName("직원 변경 시 도메인 이벤트로 조직도 캐시가 무효화된다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void employeeChange_invalidatesOrganizationChartCacheByDomainEvent() {
        Department company = createDepartment("COMP_CACHE_01", "Cache Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.save(company);
        departmentFinder.clearOrganizationChartCache();

        Cache cache = Objects.requireNonNull(cacheManager.getCache("organizationChart"));

        List<OrganizationChartDetail> chartsBefore = departmentFinder.getOrganizationChart();
        OrganizationChartDetail companyChartBefore = chartsBefore.stream()
                .filter(chart -> chart.departmentId().equals(company.getIdOrThrow()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 부서를 찾을 수 없습니다."));
        assertThat(companyChartBefore.employeeCount()).isEqualTo(0);
        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();

        employeeManager.create(EmployeeCreateCommand.builder()
                .departmentId(company.getIdOrThrow())
                .email("cache-event-test@abms.co")
                .name("캐시테스트")
                .joinDate(LocalDate.of(2024, 1, 1))
                .birthDate(LocalDate.of(1994, 1, 1))
                .position(EmployeePosition.TEAM_LEADER)
                .type(EmployeeType.FULL_TIME)
                .grade(EmployeeGrade.SENIOR)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .memo("cache invalidation event test")
                .build());

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();

        List<OrganizationChartDetail> chartsAfter = departmentFinder.getOrganizationChart();
        OrganizationChartDetail companyChartAfter = chartsAfter.stream()
                .filter(chart -> chart.departmentId().equals(company.getIdOrThrow()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 부서를 찾을 수 없습니다."));
        assertThat(companyChartAfter.employeeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("리더 변경 시 도메인 이벤트로 조직도 캐시가 무효화된다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void assignLeader_invalidatesOrganizationChartCacheByDomainEvent() {
        Department company = createDepartment("COMP_CACHE_02", "Leader Cache Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.save(company);

        Employee leader = employeeRepository.save(createEmployee(company.getId(), "leader-cache-event-test@abms.co"));
        departmentFinder.clearOrganizationChartCache();

        Cache cache = Objects.requireNonNull(cacheManager.getCache("organizationChart"));

        List<OrganizationChartDetail> chartsBefore = departmentFinder.getOrganizationChart();
        OrganizationChartDetail companyChartBefore = chartsBefore.stream()
                .filter(chart -> chart.departmentId().equals(company.getIdOrThrow()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 부서를 찾을 수 없습니다."));
        assertThat(companyChartBefore.leader()).isNull();
        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();

        departmentManager.assignLeader(company.getIdOrThrow(), leader.getIdOrThrow());

        assertThat(cache.get(SimpleKey.EMPTY)).isNull();

        List<OrganizationChartDetail> chartsAfter = departmentFinder.getOrganizationChart();
        OrganizationChartDetail companyChartAfter = chartsAfter.stream()
                .filter(chart -> chart.departmentId().equals(company.getIdOrThrow()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 부서를 찾을 수 없습니다."));
        assertThat(companyChartAfter.leader()).isNotNull();
        assertThat(companyChartAfter.leader().leaderEmployeeId()).isEqualTo(leader.getIdOrThrow());
    }

    private Employee createEmployee(Long departmentId, String email) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable Long leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
                code,
                name,
                type,
                leaderEmployeeId,
                parent);
    }

}
