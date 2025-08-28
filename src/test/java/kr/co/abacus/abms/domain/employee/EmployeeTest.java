package kr.co.abacus.abms.domain.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    Employee employee;

    @BeforeEach
    void init() {
        employee = createEmployee();
    }

    @Test
    void create() {
        assertThat(employee.getEmail().address()).isEqualTo("testUser@email.com");
        assertThat(employee.getName()).isEqualTo("홍길동");
        assertThat(employee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.MANAGER);
        assertThat(employee.getType()).isEqualTo(EmployeeType.FULL_TIME);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.SENIOR);
        assertThat(employee.getMemo()).isEqualTo("This is a memo for the employee.");
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    void resign() {
        employee.resign(LocalDate.of(2025, 12, 31));

        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(employee.getResignationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void resignAlreadyResigned() {
        employee.resign(LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> employee.resign(LocalDate.of(2026, 1, 1)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 퇴사한 직원입니다.");
    }

    @Test
    void resignBeforeJoinDate() {
        assertThatThrownBy(() -> employee.resign(LocalDate.of(2024, 12, 31)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("퇴사일은 입사일 이후여야 합니다.");
    }

    @Test
    void takeLeave() {
        employee.takeLeave();

        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    void takeLeaveFail() {
        employee.resign(LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> employee.takeLeave())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("재직 중인 직원만 휴직 처리 할 수 있습니다.");
    }

    @Test
    void update() {
        EmployeeUpdateRequest updateRequest = createEmployeeUpdateRequest();

        employee.updateInfo(updateRequest);

        assertThat(employee.getEmail().address()).isEqualTo("updateUser@email.com");
        assertThat(employee.getName()).isEqualTo("김철수");
        assertThat(employee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.DIRECTOR);
        assertThat(employee.getType()).isEqualTo(EmployeeType.PART_TIME);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.JUNIOR);
        assertThat(employee.getMemo()).isEqualTo("Updated memo for the employee.");
    }

    @Test
    void updateResignedEmployee() {
        employee.resign(LocalDate.of(2025, 12, 31));

        EmployeeUpdateRequest updateRequest = createEmployeeUpdateRequest();

        assertThatThrownBy(() -> employee.updateInfo(updateRequest))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("퇴사한 직원은 정보를 수정할 수 없습니다.");
    }

    @Test
    void activate() {
        employee.takeLeave();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);

        employee.activate();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    void activateAlreadyActive() {
        assertThatThrownBy(() -> employee.activate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 재직 중인 직원입니다.");
    }

    @Test
    void promote() {
        employee.promote(EmployeePosition.DIRECTOR);

        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.DIRECTOR);
    }

    @Test
    void promoteFail() {
        assertThatThrownBy(() -> employee.promote(EmployeePosition.STAFF))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 직급보다 낮은 직급으로 변경할 수 없습니다.");
    }

    @Test
    void promoteResignedEmployee() {
        employee.resign(LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> employee.promote(EmployeePosition.DIRECTOR))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("퇴사한 직원은 승진할 수 없습니다.");
    }

    @Test
    void softDelete() {
        employee.softDelete("adminUser");

        assertThat(employee.isDeleted()).isTrue();
        assertThat(employee.getDeletedBy()).isEqualTo("adminUser");
        assertThat(employee.getEmail().address()).startsWith("deleted.");
        assertThat(employee.getDeletedAt()).isNotNull();
    }

    @Test
    void softDeleteFail() {
        employee.softDelete("adminUser");

        assertThatThrownBy(() -> employee.softDelete("anotherUser"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 삭제된 직원입니다.");
    }

}