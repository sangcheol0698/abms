package kr.co.abacus.abms.application.department.provided;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
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

    private UUID companyId;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
    }

    @Test
    void getOrganizationChart_returnsDefaultTree() {
        // when
        OrganizationChartModel chart = departmentFinder.getOrganizationChart();

        // then: root
        assertThat(chart).isNotNull();
        assertThat(chart.departmentId()).isEqualTo(companyId);
        assertThat(chart.departmentName()).isEqualTo("테스트회사");
        assertThat(chart.departmentCode()).isEqualTo("TEST_COMPANY");
        assertThat(chart.departmentType()).isEqualTo(DepartmentType.COMPANY);
        assertThat(chart.leader()).isNull();

        // and: division level
        assertThat(chart.children()).hasSize(1);
        OrganizationChartModel division = chart.children().getFirst();
        assertThat(division.departmentName()).isEqualTo("테스트본부");
        assertThat(division.departmentCode()).isEqualTo("TEST_DIV");
        assertThat(division.departmentType()).isEqualTo(DepartmentType.DIVISION);
        assertThat(division.leader()).isNull();

        // and: team level
        assertThat(division.children()).hasSize(1);
        OrganizationChartModel team = division.children().getFirst();
        assertThat(team.departmentName()).isEqualTo("테스트팀");
        assertThat(team.departmentCode()).isEqualTo("TEST_TEAM");
        assertThat(team.departmentType()).isEqualTo(DepartmentType.TEAM);
        assertThat(team.leader()).isNull();
    }

    @Test
    void getOrganizationChard_employeeCount() {
        // given: 3 employees in each team
        employeeRepository.save(createEmployee());
        employeeRepository.save(createEmployee());
        employeeRepository.save(createEmployee());

        OrganizationChartModel organizationChart = departmentFinder.getOrganizationChart();
        int employeeCount = organizationChart.employeeCount();

        // then: 3 employees in each team
        assertThat(employeeCount).isEqualTo(3);
    }

    private Employee createEmployee() {
        return Employee.builder()
            .departmentId(UUID.randomUUID())
            .email("email@email.com")
            .name("홍길동")
            .joinDate(LocalDate.of(2020, 1, 1))
            .birthDate(LocalDate.of(1990, 1, 1))
            .position(EmployeePosition.MANAGER)
            .type(EmployeeType.FULL_TIME)
            .grade(EmployeeGrade.SENIOR)
            .avatar(EmployeeAvatar.SKY_GLOW)
            .memo("This is a memo for the employee.")
            .build();
    }

}