package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;
import java.util.UUID;

public class EmployeeFixture {

    public static Employee createEmployee() {
        return Employee.create(createEmployeeCreateRequest());
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest() {
        return createEmployeeCreateRequest("testUser@email.com", "홍길동");
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest(String email) {
        return createEmployeeCreateRequest(email, "홍길동");
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest(String email, String name) {
        return new EmployeeCreateRequest(
                UUID.randomUUID(),
                email,
                name,
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
                UUID.randomUUID(),
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