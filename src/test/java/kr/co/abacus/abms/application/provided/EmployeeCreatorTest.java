package kr.co.abacus.abms.application.provided;

import jakarta.validation.ConstraintViolationException;
import kr.co.abacus.abms.application.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.*;
import kr.co.abacus.abms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeCreatorTest extends IntegrationTestBase {

    @Autowired
    private EmployeeCreator employeeCreator;

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void create() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getDepartmentId()).isEqualTo(getDefaultDepartmentId());
    }

    @Test
    void duplicateEmail() {
        employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com")))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    void invalidName() {
        assertThatThrownBy(() -> employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com", "a".repeat(11))))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void updateInfo() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        employeeCreator.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(getTestDivisionId()));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getDepartmentId()).isEqualTo(getTestDivisionId());
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
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        // 이메일이 변경되지 않은 경우, 이메일 중복 체크를 하지 않음
        employeeCreator.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), employee.getName(), employee.getEmail().address()));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getEmail().address()).isEqualTo("testUser@email.com");
    }

    @Test
    void updateInfoFail_duplicateEmail() {
        Employee employee1 = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        Employee employee2 = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser2@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.updateInfo(employee1.getId(), createEmployeeUpdateRequestWithDepartment(employee1.getDepartmentId(), employee1.getName(), employee2.getEmail().address())))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    void updateInfoFail_invalidName() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "", "updateUser@email.com")))
            .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> employeeCreator.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "a".repeat(11), "updateUser@email.com")))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void updateInfoFail_invalidEmail() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.updateInfo(employee.getId(), createEmployeeUpdateRequestWithDepartment(employee.getDepartmentId(), "updateUser", null)))
            .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void resign() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com")); // 입사일: 2025, 1, 1
        flushAndClear();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeCreator.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        Employee resignedEmployee = employeeFinder.find(employee.getId());
        assertThat(resignedEmployee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(resignedEmployee.getResignationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void resignFail_alreadyResigned() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com")); // 입사일: 2025, 1, 1

        employeeCreator.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.resign(employee.getId(), LocalDate.of(2026, 1, 1)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 퇴사한 직원입니다.");
    }

    @Test
    void resignFail_beforeJoinDate() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com")); // 입사일: 2025, 1, 1
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.resign(employee.getId(), LocalDate.of(2024, 12, 31)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("퇴사일은 입사일 이후여야 합니다.");
    }

    @Test
    void takeLeave() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        flushAndClear();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeCreator.takeLeave(employee.getId());
        flushAndClear();

        Employee onLeaveEmployee = employeeFinder.find(employee.getId());
        assertThat(onLeaveEmployee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    void takeLeaveFail_notActive() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com"));
        employeeCreator.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.takeLeave(employee.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("재직 중인 직원만 휴직 처리 할 수 있습니다.");
    }

    @Test
    void activate() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com", "홍길동"));
        employeeCreator.resign(employee.getId(), LocalDate.of(2025, 12, 31)); // 퇴사 처리
        flushAndClear();

        employeeCreator.activate(employee.getId());
        flushAndClear();

        Employee activatedEmployee = employeeFinder.find(employee.getId());
        assertThat(activatedEmployee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(activatedEmployee.getResignationDate()).isNull();
    }

    @Test
    void activateFail_alreadyActive() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com", "홍길동"));
        flushAndClear();

        assertThatThrownBy(() -> employeeCreator.activate(employee.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 재직 중인 직원입니다.");
    }

    @Test
    void delete() {
        Employee employee = employeeCreator.create(createEmployeeCreateRequestWithDepartment(getDefaultDepartmentId(), "testUser@email.com", "홍길동"));
        flushAndClear();

        employeeCreator.delete(employee.getId(), "adminUser");
        flushAndClear();

        Employee deletedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(deletedEmployee.isDeleted()).isTrue();
        assertThat(deletedEmployee.getDeletedBy()).isEqualTo("adminUser");
        assertThat(deletedEmployee.getEmail().address()).startsWith("deleted.");
        assertThat(deletedEmployee.getDeletedAt()).isNotNull();
    }


}