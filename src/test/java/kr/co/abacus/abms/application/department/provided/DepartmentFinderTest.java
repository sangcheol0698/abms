package kr.co.abacus.abms.application.department.provided;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.support.IntegrationTestBase;

class DepartmentFinderTest extends IntegrationTestBase {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private UUID companyId;
    private UUID teamId;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
                DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
                company);
        departmentRepository.save(division);
        Department team = Department.create(
                DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
                division);
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
        teamId = team.getId();
    }

    @Test
    void find() {
        Department foundDepartment = departmentFinder.find(companyId);

        assertThat(foundDepartment.getId()).isEqualTo(companyId);
        assertThat(foundDepartment.getName()).isEqualTo("테스트회사");
        assertThat(foundDepartment.getCode()).isEqualTo("TEST_COMPANY");
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> departmentFinder.find(UUID.randomUUID()))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    @DisplayName("부서별 직원 조회 - 페이징")
    void getEmployees() {
        // Given: 부서에 직원 추가
        for (int i = 1; i <= 15; i++) {
            Employee employee = Employee.create(
                    EmployeeFixture.createEmployeeCreateRequest("emp" + i + "@test.com", "직원" + i, teamId));
            employeeRepository.save(employee);
        }
        flushAndClear();

        // When: 첫 번째 페이지 조회
        Page<Employee> result = departmentFinder.getEmployees(teamId, null, PageRequest.of(0, 10));

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

        // When: 조회
        Page<Employee> result = departmentFinder.getEmployees(companyId, null, PageRequest.of(0, 10));

        // Then: 빈 페이지 반환
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("부서별 직원 조회 - 존재하지 않는 부서")
    void getEmployees_notFoundDepartment() {
        // Given: 존재하지 않는 부서 ID
        UUID nonExistentId = UUID.randomUUID();

        // When & Then: DepartmentNotFoundException 발생
        assertThatThrownBy(() -> departmentFinder.getEmployees(nonExistentId, null, PageRequest.of(0, 10)))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

}