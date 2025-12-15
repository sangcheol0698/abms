package kr.co.abacus.abms.domain.employee;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    @Test
    @DisplayName("직원 도메인 생성")
    void create() {
        // given, when
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // then
        assertThat(employee.getEmail().address()).isEqualTo("testUser@email.com");
        assertThat(employee.getName()).isEqualTo("홍길동");
        assertThat(employee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.MANAGER);
        assertThat(employee.getType()).isEqualTo(EmployeeType.FULL_TIME);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.SENIOR);
        assertThat(employee.getAvatar()).isEqualTo(EmployeeAvatar.SKY_GLOW);
        assertThat(employee.getMemo()).isEqualTo("This is a memo for the employee.");
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("직원 퇴사 처리")
    void resign() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.resign(LocalDate.of(2025, 12, 31));

        // then
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(employee.getResignationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("이미 퇴사한 직원 퇴사 처리 시도시 예외 발생")
    void resignAlreadyResigned() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.resign(LocalDate.of(2025, 12, 31));

        // then
        assertThatThrownBy(() -> employee.resign(LocalDate.of(2026, 1, 1)))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("이미 퇴사한 직원입니다.");
    }

    @Test
    @DisplayName("퇴사일이 입사일 이전일 경우 예외 발생")
    void resignBeforeJoinDate() {
        // given, when
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // then
        assertThatThrownBy(() -> employee.resign(LocalDate.of(2024, 12, 31)))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("퇴사일은 입사일 이후여야 합니다.");
    }

    @Test
    @DisplayName("직원 휴직 처리")
    void takeLeave() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.takeLeave();

        // then
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("재직 중인 직원만 휴직 처리 가능")
    void takeLeaveFail() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.resign(LocalDate.of(2025, 12, 31));

        // then
        assertThatThrownBy(() -> employee.takeLeave())
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("재직 중인 직원만 휴직 처리 할 수 있습니다.");
    }

    @Test
    @DisplayName("직원 정보 수정")
    void update() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        UUID updatedDepartmentId = UUID.randomUUID();
        employee.updateInfo(
            updatedDepartmentId,
            "김철수",
            "updateUser@email.com",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            EmployeeAvatar.CORAL_SPARK,
            "Updated memo for the employee."
        );

        // then
        assertThat(employee.getDepartmentId()).isEqualTo(updatedDepartmentId);
        assertThat(employee.getEmail().address()).isEqualTo("updateUser@email.com");
        assertThat(employee.getName()).isEqualTo("김철수");
        assertThat(employee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(employee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.DIRECTOR);
        assertThat(employee.getType()).isEqualTo(EmployeeType.PART_TIME);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.JUNIOR);
        assertThat(employee.getAvatar()).isEqualTo(EmployeeAvatar.CORAL_SPARK);
        assertThat(employee.getMemo()).isEqualTo("Updated memo for the employee.");
    }

    @Test
    @DisplayName("퇴사한 직원은 정보 수정 불가")
    void updateResignedEmployee() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.resign(LocalDate.of(2025, 12, 31));

        // then
        assertThatThrownBy(() -> employee.updateInfo(
            UUID.randomUUID(),
            "김철수",
            "updateUser@email.com",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            EmployeeAvatar.CORAL_SPARK,
            "Updated memo for the employee."
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("퇴사한 직원은 정보를 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("휴직 중인 직원 재활성화 (휴직 -> 재직)")
    void activate() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.takeLeave();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);

        // then
        employee.activate();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 재직 중인 직원 재활성화 시도 시 예외 발생")
    void activateAlreadyActive() {
        // given, when
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // then
        assertThatThrownBy(() -> employee.activate())
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("이미 재직 중인 직원입니다.");
    }

    @Test
    @DisplayName("직원 승진")
    void promote() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.promote(EmployeePosition.DIRECTOR);

        // then
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.DIRECTOR);
    }

    @Test
    @DisplayName("현재 직급보다 낮은 직급으로 승진할 수 없다")
    void promoteFail() {
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        assertThatThrownBy(() -> employee.promote(EmployeePosition.STAFF))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("현재 직급보다 낮은 직급으로 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("퇴사한 직원은 승진할 수 없다")
    void promoteResignedEmployee() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.resign(LocalDate.of(2025, 12, 31));

        // then
        assertThatThrownBy(() -> employee.promote(EmployeePosition.DIRECTOR))
            .isInstanceOf(InvalidEmployeeStatusException.class)
            .hasMessage("퇴사한 직원은 승진할 수 없습니다.");
    }

    @Test
    @DisplayName("직원 소프트 삭제")
    void softDelete() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.softDelete("adminUser");

        // then
        assertThat(employee.isDeleted()).isTrue();
        assertThat(employee.getDeletedBy()).isEqualTo("adminUser");
        assertThat(employee.getEmail().address()).startsWith("deleted.");
        assertThat(employee.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 직원은 삭제할 수 없음")
    void softDeleteFail() {
        // given
        Employee employee = createEmployee(LocalDate.of(2025, 1, 1));

        // when
        employee.softDelete("adminUser");

        // then
        assertThatThrownBy(() -> employee.softDelete("anotherUser"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 삭제된 직원입니다.");
    }

    private Employee createEmployee(LocalDate joinDate) {
        return Employee.create(
            UUID.randomUUID(),
            "홍길동",
            "testUser@email.com",
            joinDate,
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

}
