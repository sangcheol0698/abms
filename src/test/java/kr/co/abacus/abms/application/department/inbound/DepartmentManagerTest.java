package kr.co.abacus.abms.application.department.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class DepartmentManagerTest extends IntegrationTestBase {

    @Autowired
    private DepartmentManager departmentManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("팀의 팀장을 지정한다.")
    void assignTeamLeader() {
        // given
        Department department1 = createDepartment("TEST-CODE", "테스트팀", DepartmentType.TEAM, null, null);
        departmentRepository.save(department1);

        Employee employee = createEmployee();
        employeeRepository.save(employee);

        UUID departmentId = departmentManager.assignTeamLeader(department1.getId(), employee.getId());
        flushAndClear();

        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId).orElseThrow();
        assertThat(department.getLeaderEmployeeId()).isEqualTo(employee.getId());
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable UUID leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
            code,
            name,
            type,
            leaderEmployeeId,
            parent
        );
    }

    private Employee createEmployee() {
        return Employee.create(
            UUID.randomUUID(),
            "홍길동",
            "test@email.com",
            LocalDate.of(2020, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

}