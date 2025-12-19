package kr.co.abacus.abms.application.employee.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeFinderTest extends IntegrationTestBase {

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUpDepartments() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));

        companyId = company.getId();
    }

    @Test
    void find() {
        UUID employeeId = employeeRepository.save(createEmployee(companyId, "test@email.com")).getId();
        flushAndClear();

        Employee foundEmployee = employeeFinder.find(employeeId);
        assertThat(foundEmployee.getId()).isEqualTo(employeeId);
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> employeeFinder.find(UUID.randomUUID()))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void findDeleted() {
        UUID employeeId = employeeRepository.save(createEmployee(companyId, "testUser@email.com")).getId();
        flush();

        Employee savedEmployee = employeeFinder.find(employeeId);
        savedEmployee.softDelete("testUser");
        flushAndClear();

        assertThatThrownBy(() -> employeeFinder.find(employeeId))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

    private Department createDepartment(String code, String name, DepartmentType type, UUID leaderId, Department parent) {
        return Department.create(
            code,
            name,
            type,
            leaderId,
            parent
        );
    }

    private Employee createEmployee(UUID teamId, String email) {
        return Employee.create(
            teamId,
            "홍길동",
            email,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(1990, 5, 20),
            EmployeePosition.ASSOCIATE,
            EmployeeType.FULL_TIME,
            EmployeeGrade.JUNIOR,
            EmployeeAvatar.SKY_GLOW,
            null
        );
    }

}