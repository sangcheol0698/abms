package kr.co.abacus.abms.application.department.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;
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

class DepartmentOrganizationChartTest extends IntegrationTestBase {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void getOrganizationChart_returnsDefaultTree() {
        // given
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));

        // when
        List<OrganizationChartInfo> charts = departmentFinder.getOrganizationChart();

        // then: root
        OrganizationChartInfo chart = charts.get(0);

        assertThat(chart).isNotNull();
        assertThat(chart.departmentId()).isEqualTo(company.getId());
        assertThat(chart.departmentName()).isEqualTo("ABC Corp");
        assertThat(chart.departmentCode()).isEqualTo("COMP001");
        assertThat(chart.departmentType()).isEqualTo(DepartmentType.COMPANY);
        assertThat(chart.leader()).isNull();

        // and: division level
        assertThat(chart.children()).hasSize(1);
        OrganizationChartInfo div = chart.children().getFirst();
        assertThat(div.departmentName()).isEqualTo("ABC Corp");
        assertThat(div.departmentCode()).isEqualTo("DIV001");
        assertThat(div.departmentType()).isEqualTo(DepartmentType.DIVISION);
        assertThat(div.leader()).isNull();

        // and: team level
        assertThat(div.children()).hasSize(2);
        OrganizationChartInfo team = div.children().getFirst();
        assertThat(team.departmentName()).isEqualTo("ABC Corp");
        assertThat(team.departmentCode()).isEqualTo("TEAM001");
        assertThat(team.departmentType()).isEqualTo(DepartmentType.TEAM);
        assertThat(team.leader()).isNull();
    }

    @Test
    void getOrganizationChard_employeeCount() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.save(company);

        employeeRepository.save(createEmployee(company.getId(), "test1@email.com"));
        employeeRepository.save(createEmployee(company.getId(), "test2@email.com"));
        employeeRepository.save(createEmployee(company.getId(), "test3@email.com"));

        List<OrganizationChartInfo> charts = departmentFinder.getOrganizationChart();

        // then: root
        OrganizationChartInfo chart = charts.get(0);
        int employeeCount = chart.employeeCount();

        // then: 3 employees in each team
        assertThat(employeeCount).isEqualTo(3);
    }

    private Employee createEmployee(UUID departmentId, String email) {
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
            "This is a memo for the employee."
        );
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable UUID leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
            code,
            name,
            type,
            leaderEmployeeId,
            parent
        );
    }

}