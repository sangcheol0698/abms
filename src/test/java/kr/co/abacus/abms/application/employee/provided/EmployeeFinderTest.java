package kr.co.abacus.abms.application.employee.provided;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.support.IntegrationTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
    }

    @Test
    void find() {
        Employee savedEmployee = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        Employee foundEmployee = employeeFinder.find(savedEmployee.getId());
        assertThat(foundEmployee).isEqualTo(savedEmployee);
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> employeeFinder.find(UUID.randomUUID()))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void findDeleted() {
        Employee savedEmployee = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flush();

        // Soft delete the employee
        savedEmployee.softDelete("testUser");
        flushAndClear();

        assertThatThrownBy(() -> employeeFinder.find(savedEmployee.getId()))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

}