package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;

public class EmployeeFixture {

    public static Employee createEmployee() {
        return Employee.create(createEmployeeCreateRequest("testUser@email.com"));
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest(String email) {
        return new EmployeeCreateRequest(
            email,
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
        return createEmployeeUpdateRequest("김철수", "updateUser@email.com");
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequest(String name) {
        return createEmployeeUpdateRequest(name, "updateUser@email.com");
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequest(String name, String email) {
        return new EmployeeUpdateRequest(
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            "Updated memo for the employee."
        );
    }

}