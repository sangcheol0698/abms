package kr.co.abacus.abms.application.department.provided;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeFinderTest extends IntegrationTestBase {

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeManager employeeManager;

    @Test
    void find() {
        Employee savedEmployee = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        Employee foundEmployee = employeeFinder.find(savedEmployee.getId());
        assertThat(foundEmployee).isEqualTo(savedEmployee);
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> employeeFinder.find(UUID.randomUUID()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findDeleted() {
        Employee savedEmployee = employeeManager.create(EmployeeFixture.createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flush();

        // Soft delete the employee
        savedEmployee.softDelete("testUser");
        flushAndClear();

        assertThatThrownBy(() -> employeeFinder.find(savedEmployee.getId()))
            .isInstanceOf(IllegalArgumentException.class);
    }

}