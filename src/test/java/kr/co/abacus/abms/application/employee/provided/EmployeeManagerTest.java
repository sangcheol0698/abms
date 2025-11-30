package kr.co.abacus.abms.application.employee.provided;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.employee.InvalidEmployeeStatusException;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeManagerTest extends IntegrationTestBase {

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;
    private UUID divisionId;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
        divisionId = division.getId();
    }

    @Test
    void create() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getDepartmentId()).isEqualTo(companyId);
    }

    @Test
    void duplicateEmail() {
        employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com")))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    void invalidName() {
        assertThatThrownBy(() -> employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com", "a".repeat(11))))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void updateInfo() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        employeeManager.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(divisionId));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getDepartmentId()).isEqualTo(divisionId);
        assertThat(updatedEmployee.getEmail().address()).isEqualTo("updateUser@email.com");
        assertThat(updatedEmployee.getName()).isEqualTo("김철수");
        assertThat(updatedEmployee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(updatedEmployee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(updatedEmployee.getPosition()).isEqualTo(EmployeePosition.DIRECTOR);
        assertThat(updatedEmployee.getType()).isEqualTo(EmployeeType.PART_TIME);
        assertThat(updatedEmployee.getGrade()).isEqualTo(EmployeeGrade.JUNIOR);
        assertThat(updatedEmployee.getMemo()).isEqualTo("Updated memo for the employee.");
    }

    @Test
    void updateInfo_noChangeEmail() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        // 이메일이 변경되지 않은 경우, 이메일 중복 체크를 하지 않음
        employeeManager.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), employee.getName(), employee.getEmail().address()));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getEmail().address()).isEqualTo("testUser@email.com");
    }

    @Test
    void updateInfoFail_duplicateEmail() {
        Employee employee1 = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        Employee employee2 = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser2@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.updateInfo(employee1.getId(), createEmployeeUpdateRequestWithDepartment(employee1.getDepartmentId(), employee1.getName(), employee2.getEmail().address())))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    void updateInfoFail_invalidName() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "", "updateUser@email.com")))
            .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> employeeManager.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "a".repeat(11), "updateUser@email.com")))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void updateInfoFail_invalidEmail() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "updateUser", null)))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void resign() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com")); // 입사일: 2025, 1, 1
        flushAndClear();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        Employee resignedEmployee = employeeFinder.find(employee.getId());
        assertThat(resignedEmployee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(resignedEmployee.getResignationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void resignFail_alreadyResigned() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com")); // 입사일: 2025, 1, 1

        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.resign(employee.getId(), LocalDate.of(2026, 1, 1)))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("이미 퇴사한 직원입니다.");
    }

    @Test
    void resignFail_beforeJoinDate() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com")); // 입사일: 2025, 1, 1
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.resign(employee.getId(), LocalDate.of(2024, 12, 31)))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("퇴사일은 입사일 이후여야 합니다.");
    }

    @Test
    void takeLeave() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        flushAndClear();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeManager.takeLeave(employee.getId());
        flushAndClear();

        Employee onLeaveEmployee = employeeFinder.find(employee.getId());
        assertThat(onLeaveEmployee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    void takeLeaveFail_notActive() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com"));
        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.takeLeave(employee.getId()))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("재직 중인 직원만 휴직 처리 할 수 있습니다.");
    }

    @Test
    void activate() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com", "홍길동"));
        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31)); // 퇴사 처리
        flushAndClear();

        employeeManager.activate(employee.getId());
        flushAndClear();

        Employee activatedEmployee = employeeFinder.find(employee.getId());
        assertThat(activatedEmployee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(activatedEmployee.getResignationDate()).isNull();
    }

    @Test
    void activateFail_alreadyActive() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.activate(employee.getId()))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("이미 재직 중인 직원입니다.");
    }

    @Test
    void delete() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        employeeManager.delete(employee.getId(), "adminUser");
        flushAndClear();

        Employee deletedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(deletedEmployee.isDeleted()).isTrue();
        assertThat(deletedEmployee.getDeletedBy()).isEqualTo("adminUser");
        assertThat(deletedEmployee.getEmail().address()).startsWith("deleted.");
        assertThat(deletedEmployee.getDeletedAt()).isNotNull();
    }

    @Test
    void restore() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId, "restore@email.com", "홍길동"));
        employeeManager.delete(employee.getId(), "adminUser");
        flushAndClear();

        employeeManager.restore(employee.getId());
        flushAndClear();

        Employee restoredEmployee = employeeFinder.find(employee.getId());
        assertThat(restoredEmployee.isDeleted()).isFalse();
        assertThat(restoredEmployee.getDeletedAt()).isNull();
        assertThat(restoredEmployee.getDeletedBy()).isNull();
    }

}
