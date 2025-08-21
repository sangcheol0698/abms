package kr.co.abacus.abms.application.provided;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;

@Transactional
@SpringBootTest
class EmployeeCreatorTest {

    @Autowired
    private EmployeeCreator employeeCreator;

    @Autowired
    private EntityManager entityManager;

    @Test
    void create() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequest());

        entityManager.flush();
        entityManager.clear();

        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

}