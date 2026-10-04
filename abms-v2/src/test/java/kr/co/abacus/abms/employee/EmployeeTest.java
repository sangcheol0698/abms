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

    private static EmployeeProfile profile(LocalDate joinDate, LocalDate careerStart, String skills, String phone) {
        return new EmployeeProfile(1L, "홍길동", "hong@test.co", joinDate, LocalDate.of(1990, 1, 1), EmployeePosition.SENIOR_ASSOCIATE,
                EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.SKY_GLOW, null, phone, careerStart,
                EmployeeJob.DEVELOPMENT, skills, WorkType.CLIENT_SITE);
    }

    @Test
    void 보유_기술은_대소문자를_무시하고_중복을_제거한다() {
        Employee employee = Employee.create(profile(LocalDate.of(2020, 3, 2), null, " Java, Spring ,java,\nAWS ,", "010-1234-5678"));

        assertThat(employee.getSkills()).isEqualTo("Java, Spring, AWS");
        assertThat(employee.skillList()).containsExactly("Java", "Spring", "AWS");
        assertThat(employee.getPhone()).isEqualTo("010-1234-5678");
        assertThat(employee.getJob()).isEqualTo(EmployeeJob.DEVELOPMENT);
        assertThat(employee.getWorkType()).isEqualTo(WorkType.CLIENT_SITE);
    }

    @Test
    void 경력_시작일은_입사일보다_늦을_수_없고_연락처_형식을_검증한다() {
        assertThatThrownBy(() -> Employee.create(profile(LocalDate.of(2020, 3, 2), LocalDate.of(2021, 1, 1), null, null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("경력 시작일");
        assertThatThrownBy(() -> Employee.create(profile(LocalDate.of(2020, 3, 2), null, null, "전화번호")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("연락처");
    }

    @Test
    void 총_경력은_경력_시작일부터_근속은_입사일부터_계산하고_퇴사자는_퇴사일까지_센다() {
        Employee employee = Employee.create(profile(LocalDate.of(2020, 3, 2), LocalDate.of(2015, 3, 2), null, null));

        assertThat(employee.careerMonths(LocalDate.of(2026, 3, 2))).isEqualTo(132);
        assertThat(employee.tenureMonths(LocalDate.of(2026, 3, 2))).isEqualTo(72);

        employee.resign(LocalDate.of(2025, 3, 2));
        assertThat(employee.tenureMonths(LocalDate.of(2026, 3, 2))).isEqualTo(60);
    }

}
