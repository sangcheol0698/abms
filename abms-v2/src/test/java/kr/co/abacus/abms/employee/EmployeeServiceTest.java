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

        employeeService.updateOwnProfile(self, teamMember.id(), "새이름", LocalDate.of(1991, 2, 3), EmployeeAvatar.GOLDEN_RAY);
        assertThat(teamMember.getName()).isEqualTo("새이름");

        assertThatThrownBy(() -> employeeService.update(self, teamMember.id(), profileOf(teamMember, team.id())))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.updateOwnProfile(self, manager.id(), "x", LocalDate.of(1990, 1, 1), EmployeeAvatar.SKY_GLOW))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.getForRead(self, manager.id())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 등록_시_직급_이력과_초기_연봉을_만든다() {
        LoginUser admin = Fixtures.admin(manager);
        Employee created = employeeService.create(admin, new EmployeeProfile(team.id(), "신입", "new@test.co",
                LocalDate.of(2026, 3, 2), LocalDate.of(2000, 1, 1), EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR, EmployeeAvatar.AQUA_SPLASH, null), Money.wons(45_000_000));

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

    private static EmployeeProfile profileOf(Employee e, Long departmentId) {
        return new EmployeeProfile(departmentId, e.getName(), e.getEmail(), e.getJoinDate(), e.getBirthDate(), e.getPosition(),
                e.getType(), e.getGrade(), e.getAvatar(), e.getMemo());
    }

}
