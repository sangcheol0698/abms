package kr.co.abacus.abms.application.employee.outbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class EmployeeRepositoryTest extends IntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void save() {
        Employee employee = createEmployee();

        Employee savedEmployee = employeeRepository.save(employee);
        flushAndClear();

        assertThat(employee).isEqualTo(savedEmployee);
    }

    @Test
    void existsByEmail() {
        Employee employee = createEmployee();
        employeeRepository.save(employee);
        flushAndClear();

        boolean exists = employeeRepository.existsByEmail(employee.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("soft delete 된 직원은 조회되지 않아야 한다")
    void findByIdAndDeletedFalse() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();

        // 직원이 존재함
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        assertThat(foundEmployee).isEqualTo(savedEmployee);

        // 직원 소프트 삭제
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        flush();

        // 소프트 삭제된 직원은 조회되지 않음
        assertThat(employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())).isEmpty();
    }

    @Test
    @DisplayName("soft delete 된 직원은 이메일 중복 체크의 대상이 되지 않아야 한다")
    void existsByEmail_afterSoftDelete() {
        Employee employee = createEmployee();
        Employee savedEmployee = employeeRepository.save(employee);
        flushAndClear();

        // 직원이 존재함
        boolean exists = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(exists).isTrue();

        // 직원 소프트 삭제
        Employee foundEmployee = employeeRepository.findByIdAndDeletedFalse(savedEmployee.getId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다"));
        foundEmployee.softDelete("testUser");
        employeeRepository.save(foundEmployee);
        flushAndClear();

        // 소프트 삭제된 직원은 이메일 중복체크 대상이 아님
        boolean existsAfterDelete = employeeRepository.existsByEmail(savedEmployee.getEmail());
        assertThat(existsAfterDelete).isFalse();
    }

    @Test
    @DisplayName("id list로 직원 조회")
    void findAllByIdInAndDeletedFalse() {
        Employee employee1 = createEmployee("testUser1@email.com");
        Employee employee2 = createEmployee("testUser2@email.com");
        Employee savedEmployee1 = employeeRepository.save(employee1);
        Employee savedEmployee2 = employeeRepository.save(employee2);
        flushAndClear();

        List<Employee> employees = employeeRepository.findAllByIdInAndDeletedFalse(List.of(savedEmployee1.getId(), savedEmployee2.getId()));
        assertThat(employees).containsExactlyInAnyOrder(savedEmployee1, savedEmployee2);
    }

    @Test
    @DisplayName("직원 조건에 따른 검색")
    void search() {
        UUID departmentId = departmentRepository.save(DepartmentFixture.createRootDepartment()).getId();

        employeeRepository.save(getEmployee("test1@email.com", "홍길동", EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, departmentId));
        employeeRepository.save(getEmployee("test2@email.com", "김길동", EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR, departmentId));
        employeeRepository.save(getEmployee("test3@email.com", "이길동", EmployeePosition.DIRECTOR, EmployeeType.FREELANCER, EmployeeGrade.MID_LEVEL, departmentId));

        EmployeeSearchCondition request = new EmployeeSearchCondition("길동", List.of(EmployeePosition.MANAGER, EmployeePosition.ASSOCIATE), null, null, null, null);
        Page<EmployeeSummary> employees = employeeRepository.search(request, PageRequest.of(0, 10));

        assertThat(employees).hasSize(2)
            .extracting(EmployeeSummary::name)
            .containsExactlyInAnyOrder("홍길동", "김길동");
    }

    private Employee getEmployee(String email, String name, EmployeePosition employeePosition, EmployeeType employeeType, EmployeeGrade employeeGrade, UUID departmentId) {
        return createEmployee(
            departmentId,
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            employeePosition,
            employeeType,
            employeeGrade,
            EmployeeAvatar.FOREST_MINT,
            "This is a memo for the employee."
        );
    }

    @Test
    @DisplayName("부서 ID로 직원 페이징 조회")
    void findAllByDepartmentIdAndDeletedFalse() {
        // Given: 특정 부서에 직원 생성
        UUID departmentId = UUID.randomUUID();
        UUID otherDepartmentId = UUID.randomUUID();

        Employee employee1 = createEmployee(departmentId, "emp1@test.com", "직원1", LocalDate.of(2025, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.FOREST_MINT, "memo");
        Employee employee2 = createEmployee(departmentId, "emp2@test.com", "직원2", LocalDate.of(2025, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.FOREST_MINT, "memo");
        Employee employee3 = createEmployee(otherDepartmentId, "emp3@test.com", "직원3", LocalDate.of(2025, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.FOREST_MINT, "memo");

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        flushAndClear();

        // When: 부서 ID로 직원 조회
        Page<Employee> result = employeeRepository.findAllByDepartmentIdAndDeletedFalse(
            departmentId,
            PageRequest.of(0, 10)
        );

        // Then: 해당 부서 직원만 조회됨
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting(Employee::getName)
            .containsExactlyInAnyOrder("직원1", "직원2");
    }

    @Test
    @DisplayName("부서 ID로 직원 조회 시 soft delete된 직원은 제외")
    void findAllByDepartmentIdAndDeletedFalse_excludesDeleted() {
        // Given: 부서에 직원 생성 후 일부 삭제
        UUID departmentId = UUID.randomUUID();

        Employee activeEmployee = createEmployee(departmentId, "active@test.com", "활성직원", LocalDate.of(2025, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.FOREST_MINT, "memo");
        Employee deletedEmployee = createEmployee(departmentId, "deleted@test.com", "삭제직원", LocalDate.of(2025, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.FOREST_MINT, "memo");

        employeeRepository.save(activeEmployee);
        employeeRepository.save(deletedEmployee);
        flushAndClear();

        // 직원 soft delete
        Employee toDelete = employeeRepository.findById(deletedEmployee.getId()).orElseThrow();
        toDelete.softDelete("testUser");
        employeeRepository.save(toDelete);
        flushAndClear();

        // When: 부서 ID로 직원 조회
        Page<Employee> result = employeeRepository.findAllByDepartmentIdAndDeletedFalse(
            departmentId,
            PageRequest.of(0, 10)
        );

        // Then: 활성 직원만 조회됨
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("활성직원");
    }

    @Test
    @DisplayName("부서에 직원이 없을 때 빈 페이지 반환")
    void findAllByDepartmentIdAndDeletedFalse_emptyResult() {
        // Given: 직원이 없는 부서 ID
        UUID emptyDepartmentId = UUID.randomUUID();

        // When: 조회
        Page<Employee> result = employeeRepository.findAllByDepartmentIdAndDeletedFalse(
            emptyDepartmentId,
            PageRequest.of(0, 10)
        );

        // Then: 빈 페이지 반환
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    private Employee createEmployee(UUID departmentId, String email, String name, LocalDate joinDate,
                                    LocalDate birthDate, EmployeePosition position, EmployeeType type,
                                    EmployeeGrade grade, EmployeeAvatar avatar, String memo) {
        return Employee.builder()
            .departmentId(departmentId)
            .email(email)
            .name(name)
            .joinDate(joinDate)
            .birthDate(birthDate)
            .position(position)
            .type(type)
            .grade(grade)
            .avatar(avatar)
            .memo(memo)
            .build();
    }

    private Employee createEmployee() {
        return createEmployee(
            UUID.randomUUID(),
            "testUser@email.com",
            "홍길동",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

    private Employee createEmployee(String email) {
        return createEmployee(
            UUID.randomUUID(),
            email,
            "홍길동",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

}