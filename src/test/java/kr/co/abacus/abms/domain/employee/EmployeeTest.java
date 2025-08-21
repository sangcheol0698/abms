package kr.co.abacus.abms.domain.employee;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    @Test
    void create() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
            "testUser@email.com",
            "홍길동",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeStatus.ACTIVE,
            EmployeeGrade.JUNIOR
        );

        Employee employee = Employee.create(request);

        assertThat(employee.getEmail().address()).isEqualTo("testUser@email.com");
        assertThat(employee.getName()).isEqualTo("홍길동");
        assertThat(employee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.MANAGER);
        assertThat(employee.getType()).isEqualTo(EmployeeType.FULL_TIME);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

}