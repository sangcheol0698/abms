package kr.co.abacus.abms.application.employee.required;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeRepositoryTest extends IntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void save() {
        Employee employee = createEmployee();

        Employee savedEmployee = employeeRepository.save(employee);
        flushAndClear();

        assertThat(employee).isEqualTo(savedEmployee);
    }

    @Test
    void existsByEmail() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);
        flushAndClear();

        boolean exists = employeeRepository.existsByEmail(employee.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("soft delete 된 직원은 조회되지 않아야 한다")
    void findByIdAndDeletedFalse() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();

        // 직원이 존재함
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        assertThat(foundEmployee).isEqualTo(savedEmployee);

        // 직원 소프트 삭제
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        flush();

        // 소프트 삭제된 직원은 조회되지 않음
        assertThat(employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 직원은 이메일 중복 체크의 대상이 되지 않아야 한다")
    void existsByEmail_afterSoftDelete() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        flushAndClear();

        // 직원이 존재함
        boolean exists = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(exists).isTrue();

        // 직원 소프트 삭제
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        flushAndClear();

        // 소프트 삭제된 직원은 이메일 중복체크 대상이 아님
        boolean existsAfterDelete = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(existsAfterDelete).isFalse();
    }

    @Test
    @DisplayName("id list로 직원 조회")
    void findAllByIdInAndDeletedFalse() {
        Employee employee1 = createEmployee("testUser1@email.com");
        Employee employee2 = createEmployee("testUser2@email.com");
        Employee savedEmployee1 = employeeRepository.save(employee1);
        Employee savedEmployee2 = employeeRepository.save(employee2);
        flushAndClear();

        List<Employee> employees = employeeRepository.findAllByIdInAndDeletedFalse(List.of(savedEmployee1.getId(), savedEmployee2.getId()));
        assertThat(employees).containsExactlyInAnyOrder(savedEmployee1, savedEmployee2);
    }

    @Test
    @DisplayName("직원 조건에 따른 검색")
    void search() {
        employeeRepository.save(getEmployee("test1@email.com", "홍길동", EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR));
        employeeRepository.save(getEmployee("test2@email.com", "김길동", EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR));
        employeeRepository.save(getEmployee("test3@email.com", "이길동", EmployeePosition.DIRECTOR, EmployeeType.FREELANCER, EmployeeGrade.MID_LEVEL));

        Page<Employee> employees = employeeRepository.search(new EmployeeSearchRequest("길동", List.of(EmployeePosition.MANAGER, EmployeePosition.ASSOCIATE), null, null, null, null), PageRequest.of(0, 10));

        assertThat(employees).hasSize(2)
            .extracting(Employee::getName)
            .containsExactlyInAnyOrder("홍길동", "김길동");
    }

    private Employee getEmployee(String email, String name, EmployeePosition employeePosition, EmployeeType employeeType, EmployeeGrade employeeGrade) {
        return Employee.create(new EmployeeCreateRequest(
            UUID.randomUUID(),
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            employeePosition,
            employeeType,
            employeeGrade,
            EmployeeAvatar.FOREST_MINT,
            "This is a memo for the employee."
        ));
    }

}