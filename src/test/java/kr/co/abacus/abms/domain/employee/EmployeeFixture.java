package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;

public class EmployeeFixture {

    public static Employee createEmployee() {
        return Employee.create(createEmployeeCreateRequest());
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest() {
        return new EmployeeCreateRequest(
            "testUser@email.com",
            "홍길동",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            "This is a memo for the employee."
        );
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequest() {
        return new EmployeeUpdateRequest(
            "testUser@email.com",
            "김철수",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            "Updated memo for the employee."
        );
    }

}
