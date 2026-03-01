package kr.co.abacus.abms.application.employee.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryManager;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.employee.InvalidEmployeeStatusException;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("직원 관리 (EmployeeManager)")
class EmployeeManagerTest extends IntegrationTestBase {

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private EmployeeFinder employeeFinder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionHistoryManager positionHistoryManager;

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    private Long companyId;
    private Long divisionId;

    @BeforeEach
    void setUpDepartments() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        Department team2 = createDepartment("TEAM002", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1, team2));

        companyId = company.getId();
        divisionId = division.getId();
    }

    @Test
    @DisplayName("신규 직원을 등록한다")
    void create() {
        EmployeeCreateCommand command = EmployeeCreateCommand.builder()
                .departmentId(divisionId)
                .email("test@email.com")
                .name("홍길동")
                .joinDate(LocalDate.of(2025, 1, 1))
                .birthDate(LocalDate.of(1990, 1, 1))
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .grade(EmployeeGrade.JUNIOR)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .build();

        Long employeeId = employeeManager.create(command);
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);

        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getDepartmentId()).isEqualTo(divisionId);
    }

    @Test
    @DisplayName("신규 직원을 등록 시, 직급 이력도 저장된다")
    void createWithPositionHistory() {
        EmployeeCreateCommand command = EmployeeCreateCommand.builder()
                .departmentId(divisionId)
                .email("test@email.com")
                .name("홍길동")
                .joinDate(LocalDate.of(2025, 1, 1))
                .birthDate(LocalDate.of(1990, 1, 1))
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .grade(EmployeeGrade.JUNIOR)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .build();

        Long employeeId = employeeManager.create(command);
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);

        assertThat(employee.getId()).isNotNull();
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getDepartmentId()).isEqualTo(divisionId);

        /**
         * 직급 이력도 함께 저장됐는지 확인한다
         */
        PositionHistory foundPositionHistory = positionHistoryRepository.findByEmployeeId(employee.getId()).getLast();

        assertThat(employee.getId()).isEqualTo(foundPositionHistory.getEmployeeId());
        assertThat(employee.getPosition()).isEqualTo(foundPositionHistory.getPosition());
    }

    @Test
    @DisplayName("중복된 이메일로 직원 생성 시 예외가 발생한다")
    void duplicateEmail() {
        employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com")))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    @DisplayName("직원 정보를 수정한다")
    void updateInfo() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.updateInfo(employee.getId(), createEmployeeUpdateCommand(divisionId, "updateUser@email.com"));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getDepartmentId()).isEqualTo(divisionId);
        assertThat(updatedEmployee.getEmail().address()).isEqualTo("updateUser@email.com");
        assertThat(updatedEmployee.getName()).isEqualTo("홍길동");
        assertThat(updatedEmployee.getJoinDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(updatedEmployee.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(updatedEmployee.getPosition()).isEqualTo(EmployeePosition.ASSOCIATE);
        assertThat(updatedEmployee.getType()).isEqualTo(EmployeeType.FULL_TIME);
        assertThat(updatedEmployee.getGrade()).isEqualTo(EmployeeGrade.JUNIOR);
        assertThat(updatedEmployee.getMemo()).isEqualTo("Updated memo for the employee.");
    }

    @Test
    @DisplayName("이메일 변경 없이 직원 정보 수정 시 중복 체크를 하지 않는다")
    void updateInfo_noChangeEmail() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));
        flushAndClear();

        // 이메일이 변경되지 않은 경우, 이메일 중복 체크를 하지 않음
        Employee employee = employeeFinder.find(employeeId);
        employeeManager.updateInfo(employee.getId(), createEmployeeUpdateCommand(employee.getDepartmentId(),
                employee.getName(), employee.getEmail().address()));
        flushAndClear();

        Employee updatedEmployee = employeeFinder.find(employee.getId());
        assertThat(updatedEmployee.getEmail().address()).isEqualTo("testUser@email.com");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 직원 정보 수정 시 예외가 발생한다")
    void updateInfoFail_duplicateEmail() {

        Long employeeId1 = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));
        Long employeeId2 = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser2@email.com"));
        flushAndClear();

        Employee employee1 = employeeFinder.find(employeeId1);
        Employee employee2 = employeeFinder.find(employeeId2);
        assertThatThrownBy(() -> employeeManager.updateInfo(employee1.getId(),
                createEmployeeUpdateCommand(employee1.getDepartmentId(), employee2.getEmail().address())))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");
    }

    @Test
    @DisplayName("직원을 퇴사 처리한다")
    void resign() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com")); // 입사일:
        // 2025,
        // 1, 1
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        Employee resignedEmployee = employeeFinder.find(employee.getId());
        assertThat(resignedEmployee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(resignedEmployee.getResignationDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    @DisplayName("이미 퇴사한 직원을 다시 퇴사 처리할 수 없다")
    void resignFail_alreadyResigned() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com")); // 입사일:
        // 2025,
        // 1, 1

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.resign(employee.getId(), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(InvalidEmployeeStatusException.class)
                .hasMessage("이미 퇴사한 직원입니다.");
    }

    @Test
    @DisplayName("입사일 이전 날짜로 퇴사 처리할 수 없다")
    void resignFail_beforeJoinDate() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com")); // 입사일:
        // 2025,
        // 1, 1
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        assertThatThrownBy(() -> employeeManager.resign(employee.getId(), LocalDate.of(2024, 12, 31)))
                .isInstanceOf(InvalidEmployeeStatusException.class)
                .hasMessage("퇴사일은 입사일 이후여야 합니다.");
    }

    @Test
    @DisplayName("직원을 휴직 처리한다")
    void takeLeave() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);

        employeeManager.takeLeave(employee.getId());
        flushAndClear();

        Employee onLeaveEmployee = employeeFinder.find(employee.getId());
        assertThat(onLeaveEmployee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("재직 중이 아닌 직원은 휴직 처리할 수 없다")
    void takeLeaveFail_notActive() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com"));

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31));
        flushAndClear();

        assertThatThrownBy(() -> employeeManager.takeLeave(employee.getId()))
                .isInstanceOf(InvalidEmployeeStatusException.class)
                .hasMessage("재직 중인 직원만 휴직 처리 할 수 있습니다.");
    }

    @Test
    @DisplayName("퇴사한 직원을 복직(재활성) 처리한다")
    void activate() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.resign(employee.getId(), LocalDate.of(2025, 12, 31)); // 퇴사 처리
        flushAndClear();

        employeeManager.activate(employee.getId());
        flushAndClear();

        Employee activatedEmployee = employeeFinder.find(employee.getId());
        assertThat(activatedEmployee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(activatedEmployee.getResignationDate()).isNull();
    }

    @Test
    @DisplayName("직원이 사원에서 선임으로 승진한다")
    void promote() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.promote(employee.getId(), EmployeePosition.SENIOR_ASSOCIATE, null); // 승진
        flushAndClear();

        assertThat(employee.getPosition()).isEqualTo(EmployeePosition.SENIOR_ASSOCIATE);
    }

    @Test
    @DisplayName("직원 승진 시, 직급 이력도 생성된다")
    void promoteWithPositionHistory() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);

        /**
         * 승진 시, 직급 이력 생성
         */
        employeeManager.promote(employee.getId(), EmployeePosition.SENIOR_ASSOCIATE, null); // 승진
        flushAndClear();

        PositionHistory foundPositionHistory = positionHistoryRepository.findByEmployeeId(employee.getId()).getLast();
        assertThat(employee.getPosition()).isEqualTo(foundPositionHistory.getPosition());
    }

    @Test
    @DisplayName("이미 재직 중인 직원은 복직 처리할 수 없다")
    void activateFail_alreadyActive() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        assertThatThrownBy(() -> employeeManager.activate(employee.getId()))
                .isInstanceOf(InvalidEmployeeStatusException.class)
                .hasMessage("이미 재직 중인 직원입니다.");
    }

    @Test
    @DisplayName("직원을 논리 삭제(soft delete)한다")
    void delete() {
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "testUser@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
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
        Long employeeId = employeeManager.create(createEmployeeCreateCommand(companyId, "restore@email.com", "홍길동"));
        flushAndClear();

        Employee employee = employeeFinder.find(employeeId);
        employeeManager.delete(employee.getId(), "adminUser");
        flushAndClear();

        employeeManager.restore(employee.getId());
        flushAndClear();

        Employee restoredEmployee = employeeFinder.find(employee.getId());
        assertThat(restoredEmployee.isDeleted()).isFalse();
        assertThat(restoredEmployee.getDeletedAt()).isNull();
        assertThat(restoredEmployee.getDeletedBy()).isNull();
    }

    private Department createDepartment(String code, String name, DepartmentType type, Long leaderId,
                                        Department parent) {
        return Department.create(
                code,
                name,
                type,
                leaderId,
                parent);
    }

    private EmployeeCreateCommand createEmployeeCreateCommand(Long teamId, String email) {
        return EmployeeCreateCommand.builder()
                .departmentId(teamId)
                .email(email)
                .name("홍길동")
                .joinDate(LocalDate.of(2025, 1, 30))
                .birthDate(LocalDate.of(1990, 1, 1))
                .grade(EmployeeGrade.JUNIOR)
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .build();
    }

    private EmployeeCreateCommand createEmployeeCreateCommand(Long departmentId, String email, String name) {
        return EmployeeCreateCommand.builder()
                .departmentId(departmentId)
                .email(email)
                .name(name)
                .joinDate(LocalDate.of(2010, 1, 30))
                .birthDate(LocalDate.of(1990, 1, 1))
                .grade(EmployeeGrade.JUNIOR)
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .build();
    }

    private EmployeeUpdateCommand createEmployeeUpdateCommand(Long teamId, String email) {
        return EmployeeUpdateCommand.builder()
                .departmentId(teamId)
                .email(email)
                .name("홍길동")
                .joinDate(LocalDate.of(2025, 1, 1))
                .birthDate(LocalDate.of(1990, 1, 1))
                .grade(EmployeeGrade.JUNIOR)
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .memo("Updated memo for the employee.")
                .build();
    }

    private EmployeeUpdateCommand createEmployeeUpdateCommand(Long teamId, String name, String email) {
        return EmployeeUpdateCommand.builder()
                .departmentId(teamId)
                .email(email)
                .name(name)
                .joinDate(LocalDate.of(2010, 1, 30))
                .birthDate(LocalDate.of(1990, 1, 1))
                .grade(EmployeeGrade.JUNIOR)
                .position(EmployeePosition.ASSOCIATE)
                .type(EmployeeType.FULL_TIME)
                .avatar(EmployeeAvatar.SKY_GLOW)
                .build();
    }

}
