package kr.co.abacus.abms.application.provided;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;

@Transactional
@SpringBootTest
class EmployeeFinderTest {

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeCreator employeeCreator;

    @Autowired
    private EntityManager entityManager;

    @Test
    void find() {
        Employee savedEmployee = employeeCreator.create(EmployeeFixture.createEmployeeCreateRequest());
        entityManager.flush();
        entityManager.clear();

        Employee foundEmployee = employeeFinder.find(savedEmployee.getId());
        assertEquals(savedEmployee, foundEmployee);
    }

    @Test
    void findNotFound() {
        assertThrows(IllegalArgumentException.class, () -> employeeFinder.find(UUID.randomUUID()));
    }

    @Test
    void findDeleted() {
        Employee savedEmployee = employeeCreator.create(EmployeeFixture.createEmployeeCreateRequest());
        entityManager.flush();

        // Soft delete the employee
        savedEmployee.softDelete("testUser");
        entityManager.flush();
        entityManager.clear();

        assertThrows(IllegalArgumentException.class, () -> employeeFinder.find(savedEmployee.getId()));
    }

}