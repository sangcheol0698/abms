package kr.co.abacus.abms.application.employee.provided;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeFinderTest extends IntegrationTestBase {

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;

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
    void find() {
        UUID employeeId = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        Employee foundEmployee = employeeFinder.find(employeeId);
        assertThat(foundEmployee.getId()).isEqualTo(employeeId);
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> employeeFinder.find(UUID.randomUUID()))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void findDeleted() {
        UUID employeeId = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flush();

        Employee savedEmployee = employeeFinder.find(employeeId);
        savedEmployee.softDelete("testUser");
        flushAndClear();

        assertThatThrownBy(() -> employeeFinder.find(employeeId))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

}