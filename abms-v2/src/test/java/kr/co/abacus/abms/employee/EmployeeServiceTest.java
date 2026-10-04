package kr.co.abacus.abms.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    @Autowired
    private Fixtures fixtures;

    private Department division;
    private Department team;
    private Department otherDivision;
    private Employee manager;
    private Employee teamMember;
    private Employee outsider;

    @BeforeEach
    void setUp() {
        Department root = fixtures.department("회사", null);
        division = fixtures.department("본부", root);
        team = fixtures.department("팀", division);
        otherDivision = fixtures.department("다른 본부", root);
        manager = fixtures.employee(division, "본부장");
        teamMember = fixtures.employee(team, "팀원");
        outsider = fixtures.employee(otherDivision, "외부인");
    }

    @Test
    void 부서_트리_범위로_하위_부서_직원을_관리할_수_있다() {
        LoginUser user = Fixtures.user(manager, Fixtures.grants(PermissionScope.OWN_DEPARTMENT_TREE,
                PermissionCode.EMPLOYEE_READ, PermissionCode.EMPLOYEE_WRITE));

        assertThat(employeeService.getForRead(user, teamMember.id())).isEqualTo(teamMember);
        employeeService.takeLeave(user, teamMember.id());
        assertThat(teamMember.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);

        assertThatThrownBy(() -> employeeService.getForRead(user, outsider.id())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.takeLeave(user, outsider.id())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 범위_밖_부서로_직원을_옮길_수_없다() {
        LoginUser user = Fixtures.user(manager, Fixtures.grants(PermissionScope.OWN_DEPARTMENT_TREE, PermissionCode.EMPLOYEE_WRITE));
        EmployeeProfile moved = profileOf(teamMember, otherDivision.id());

        assertThatThrownBy(() -> employeeService.update(user, teamMember.id(), moved)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 본인_범위는_자기_프로필만_수정할_수_있다() {
        LoginUser self = Fixtures.user(teamMember, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ, PermissionCode.EMPLOYEE_WRITE));

        employeeService.updateOwnProfile(self, teamMember.id(), "새이름", LocalDate.of(1991, 2, 3));
        assertThat(teamMember.getName()).isEqualTo("새이름");

        assertThatThrownBy(() -> employeeService.update(self, teamMember.id(), profileOf(teamMember, team.id())))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.updateOwnProfile(self, manager.id(), "x", LocalDate.of(1990, 1, 1)))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.getForRead(self, manager.id())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 등록_시_직급_이력과_초기_연봉을_만든다() {
        LoginUser admin = Fixtures.admin(manager);
        Employee created = employeeService.create(admin, new EmployeeProfile(team.id(), "신입", "new@test.co",
                LocalDate.of(2026, 3, 2), LocalDate.of(2000, 1, 1), EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR, null), Money.wons(45_000_000));

        assertThat(positionHistoryRepository.findAllByEmployeeIdOrderByPeriodStartDateDesc(created.id())).hasSize(1);
        assertThat(payrollRepository.findEffective(created.id(), LocalDate.of(2026, 3, 2)))
                .hasValueSatisfying(p -> assertThat(p.getAnnualSalary()).isEqualTo(Money.wons(45_000_000)));
    }

    @Test
    void 이메일은_중복될_수_없다() {
        LoginUser admin = Fixtures.admin(manager);
        assertThatThrownBy(() -> employeeService.create(admin, profileOf(teamMember, team.id()), null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미 사용 중인 이메일");
    }

    @Test
    void 새_연봉을_등록하면_이전_연봉은_전날로_종료된다() {
        LoginUser admin = Fixtures.admin(manager);
        fixtures.payroll(teamMember, 50_000_000, LocalDate.of(2025, 1, 1));

        employeeService.changeSalary(admin, teamMember.id(), Money.wons(55_000_000), LocalDate.of(2026, 1, 1));

        List<Payroll> payrolls = employeeService.payrolls(teamMember.id());
        assertThat(payrolls).hasSize(2);
        assertThat(payrolls.get(1).getPeriod().endDate()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(payrollRepository.findEffective(teamMember.id(), LocalDate.of(2025, 12, 31)))
                .hasValueSatisfying(p -> assertThat(p.getAnnualSalary()).isEqualTo(Money.wons(50_000_000)));
        assertThat(payrollRepository.findEffective(teamMember.id(), LocalDate.of(2026, 6, 1)))
                .hasValueSatisfying(p -> assertThat(p.getAnnualSalary()).isEqualTo(Money.wons(55_000_000)));
    }

    @Test
    void 삭제한_직원을_복구한다() {
        LoginUser admin = Fixtures.admin(manager);
        String email = teamMember.getEmail();

        employeeService.delete(admin, teamMember.id());
        assertThat(employeeService.search(admin, EmployeeSearch.empty(), org.springframework.data.domain.Pageable.unpaged()))
                .doesNotContain(teamMember);

        employeeService.restore(admin, teamMember.id());
        assertThat(teamMember.getEmail()).isEqualTo(email);
    }

    @Test
    void 프로젝트_투입_이력이_있는_직원은_삭제할_수_없다() {
        LoginUser admin = Fixtures.admin(manager);
        Project project = fixtures.project(team, 100_000_000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        fixtures.assign(project, teamMember, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));

        assertThatThrownBy(() -> employeeService.delete(admin, teamMember.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("퇴사 처리");
        assertThat(teamMember.isDeleted()).isFalse();
    }

    @Test
    void 보유_기술로_직원을_검색하고_본인은_연락처와_기술을_수정할_수_있다() {
        LoginUser admin = Fixtures.admin(manager);
        Employee dev = employeeService.create(admin, new EmployeeProfile(team.id(), "개발자", "dev2@test.co",
                LocalDate.of(2024, 3, 4), LocalDate.of(1995, 1, 1), EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.MID_LEVEL, null, "010-1111-2222", LocalDate.of(2020, 3, 2),
                EmployeeJob.DEVELOPMENT, "Kotlin, Spring", WorkType.CLIENT_SITE), null);

        assertThat(employeeService.search(admin, new EmployeeSearch("kotlin", null, null, null, null, false),
                org.springframework.data.domain.Pageable.unpaged())).containsExactly(dev);

        LoginUser self = Fixtures.user(dev, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ, PermissionCode.EMPLOYEE_WRITE));
        employeeService.updateOwnProfile(self, dev.id(), "개발자", LocalDate.of(1995, 1, 1),
                "010-3333-4444", "Kotlin, Spring, Kafka");

        assertThat(dev.getPhone()).isEqualTo("010-3333-4444");
        assertThat(dev.skillList()).containsExactly("Kotlin", "Spring", "Kafka");
        assertThat(dev.getCareerStartDate()).isEqualTo(LocalDate.of(2020, 3, 2));
    }

    private static EmployeeProfile profileOf(Employee e, Long departmentId) {
        return new EmployeeProfile(departmentId, e.getName(), e.getEmail(), e.getJoinDate(), e.getBirthDate(), e.getPosition(),
                e.getType(), e.getGrade(), e.getMemo());
    }

}
