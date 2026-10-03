package kr.co.abacus.abms.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.BusinessException;

class EmployeeTest {

    private static Employee newEmployee() {
        return Employee.create(new EmployeeProfile(1L, "홍길동", "Hong@Test.co", LocalDate.of(2020, 3, 2),
                LocalDate.of(1990, 1, 1), EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.MID_LEVEL, EmployeeAvatar.SKY_GLOW, "  "));
    }

    @Test
    void 생성_시_재직_상태이고_이메일은_소문자로_정규화된다() {
        Employee employee = newEmployee();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getEmail()).isEqualTo("hong@test.co");
        assertThat(employee.getMemo()).isNull();
    }

    @Test
    void 잘못된_이메일은_거부한다() {
        assertThatThrownBy(() -> Employee.create(new EmployeeProfile(1L, "a", "not-email", LocalDate.now(), LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 퇴사일은_입사일_이후여야_하고_퇴사자는_수정할_수_없다() {
        Employee employee = newEmployee();
        assertThatThrownBy(() -> employee.resign(LocalDate.of(2020, 3, 2))).isInstanceOf(BusinessException.class);

        employee.resign(LocalDate.of(2026, 1, 31));
        assertThat(employee.isResigned()).isTrue();
        assertThatThrownBy(() -> employee.promote(EmployeePosition.PRINCIPAL, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> employee.updateOwnProfile("새이름", LocalDate.of(1990, 1, 1), EmployeeAvatar.GOLDEN_RAY))
                .isInstanceOf(BusinessException.class);

        employee.activate();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getResignationDate()).isNull();
    }

    @Test
    void 휴직은_재직_중인_직원만_가능하다() {
        Employee employee = newEmployee();
        employee.takeLeave();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
        assertThatThrownBy(employee::takeLeave).isInstanceOf(BusinessException.class);
    }

    @Test
    void 낮은_직급이나_같은_직급_등급으로는_승진할_수_없다() {
        Employee employee = newEmployee();
        assertThatThrownBy(() -> employee.promote(EmployeePosition.ASSOCIATE, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> employee.promote(EmployeePosition.SENIOR_ASSOCIATE, EmployeeGrade.MID_LEVEL)).isInstanceOf(BusinessException.class);

        employee.promote(EmployeePosition.SENIOR_ASSOCIATE, EmployeeGrade.SENIOR);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.SENIOR);
        employee.promote(EmployeePosition.PRINCIPAL, null);
        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.PRINCIPAL);
        assertThat(employee.getGrade()).isEqualTo(EmployeeGrade.SENIOR);
    }

    @Test
    void 삭제하면_이메일을_마스킹하고_복구하면_원래대로_돌린다() {
        Employee employee = newEmployee();
        employee.softDelete(1L);
        assertThat(employee.isDeleted()).isTrue();
        assertThat(employee.getEmail()).startsWith("deleted.").endsWith(".hong@test.co");
        assertThat(employee.originalEmail()).isEqualTo("hong@test.co");

        employee.restore();
        assertThat(employee.isDeleted()).isFalse();
        assertThat(employee.getEmail()).isEqualTo("hong@test.co");
    }

}
