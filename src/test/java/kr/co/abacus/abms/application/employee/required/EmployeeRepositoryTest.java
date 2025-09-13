package kr.co.abacus.abms.application.employee.required;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.domain.employee.Employee;

@Transactional
@SpringBootTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save() {
        Employee employee = createEmployee();

        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        assertThat(employee).isEqualTo(savedEmployee);
    }

    @Test
    void existsByEmail() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        boolean exists = employeeRepository.existsByEmail(employee.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("soft delete 된 직원은 조회되지 않아야 한다")
    void findByIdAndDeletedFalse() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        // 직원이 존재함
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        assertThat(foundEmployee).isEqualTo(savedEmployee);

        // 직원 소프트 삭제
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        entityManager.flush();
        entityManager.clear();

        // 소프트 삭제된 직원은 조회되지 않음
        assertThat(employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 직원은 이메일 중복 체크의 대상이 되지 않아야 한다")
    void existsByEmail_afterSoftDelete() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();
        entityManager.clear();

        // 직원이 존재함
        boolean exists = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(exists).isTrue();

        // 직원 소프트 삭제
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        entityManager.flush();
        entityManager.clear();

        // 소프트 삭제된 직원은 이메일 중복체크 대상이 아님
        boolean existsAfterDelete = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(existsAfterDelete).isFalse();
    }

}