package kr.co.abacus.abms.application.department.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
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

    private Employee createEmployee(Long departmentId, String email) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.MANAGER,
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