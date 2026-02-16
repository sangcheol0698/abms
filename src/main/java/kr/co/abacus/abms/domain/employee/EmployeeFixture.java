package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;


public class EmployeeFixture {

    public static Employee createEmployee(String name, String email, EmployeeType type) {
        return Employee.create(
            1L,
            name,
            email,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.ASSOCIATE,
            type,
            EmployeeGrade.JUNIOR,
            EmployeeAvatar.LAVENDER_MOON,
            null
        );
    }

}
