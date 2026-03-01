package kr.co.abacus.abms.application.department.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("부서 조회 (DepartmentFinder)")
class DepartmentFinderTest extends IntegrationTestBase {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("부서 ID로 상세 정보를 조회한다")
    void find() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.saveAll(List.of(company));

        Department foundDepartment = departmentFinder.find(company.getId());

        assertThat(foundDepartment.getId()).isEqualTo(company.getId());
        assertThat(foundDepartment.getCode()).isEqualTo("COMP001");
        assertThat(foundDepartment.getName()).isEqualTo("ABC Corp");
    }

    @Test
    @DisplayName("존재하지 않는 부서 조회 시 예외가 발생한다")
    void findNotFound() {
        assertThatThrownBy(() -> departmentFinder.find(9999L))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    @DisplayName("부서별 직원 조회 - 페이징")
    void getEmployees() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        for (int i = 1; i <= 15; i++) {
            Employee employee = createEmployee(team1.getId(), "emp" + i + "@test.com");

            employeeRepository.save(employee);
        }
        flushAndClear();

        // When: 첫 번째 페이지 조회
        Page<EmployeeSummary> result = departmentFinder.getEmployees(team1.getId(), null, PageRequest.of(0, 10));

        // Then: 페이징 정보 검증
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("부서별 직원 조회 - 빈 결과")
    void getEmployees_emptyResult() {
        // Given: 직원이 없는 부서 (company는 직원 없음)
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        departmentRepository.saveAll(List.of(company));

        // When: 조회
        Page<EmployeeSummary> result = departmentFinder.getEmployees(company.getId(), null, PageRequest.of(0, 10));

        // Then: 빈 페이지 반환
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("부서별 직원 조회 - 존재하지 않는 부서")
    void getEmployees_notFoundDepartment() {
        // Given: 존재하지 않는 부서 ID
        Long nonExistentId = 9999L;

        // When & Then: DepartmentNotFoundException 발생
        assertThatThrownBy(() -> departmentFinder.getEmployees(nonExistentId, null, PageRequest.of(0, 10)))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    private Employee createEmployee(Long departmentId, String email) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable Long leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
                code,
                name,
                type,
                leaderEmployeeId,
                parent);
    }

}