package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentFixture;

public class EmployeeFixture {

    public static Employee createEmployee() {
        return Employee.create(createEmployeeCreateRequest());
    }

    public static Employee createEmployee(String email) {
        return Employee.create(createEmployeeCreateRequest(email));
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest() {
        return createEmployeeCreateRequest("testUser@email.com", "홍길동", UUID.randomUUID());
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest(String email) {
        return createEmployeeCreateRequest(email, "홍길동", UUID.randomUUID());
    }

    public static EmployeeCreateRequest createEmployeeCreateRequest(String email, String name, UUID departmentId) {
        return new EmployeeCreateRequest(
            departmentId,
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            "This is a memo for the employee."
        );
    }

    // 통합 테스트용 메서드들 - 유효한 부서 ID 사용
    public static EmployeeCreateRequest createEmployeeCreateRequestWithDepartment(UUID departmentId) {
        return createEmployeeCreateRequestWithDepartment(departmentId, "testUser@email.com", "홍길동");
    }

    public static EmployeeCreateRequest createEmployeeCreateRequestWithDepartment(UUID departmentId, String email) {
        return createEmployeeCreateRequestWithDepartment(departmentId, email, "홍길동");
    }

    public static EmployeeCreateRequest createEmployeeCreateRequestWithDepartment(UUID departmentId, String email, String name) {
        return createEmployeeCreateRequest(email, name, departmentId);
    }

    // 기본 테스트 부서를 사용하는 편의 메서드 - 주의: 통합 테스트에서는 IntegrationTestBase의 getDefaultDepartmentId()를 직접 사용하세요
    // 도메인 테스트에서만 사용 권장
    public static EmployeeCreateRequest createEmployeeCreateRequestWithDefaultDepartment() {
        return createEmployeeCreateRequestWithDepartment(DepartmentFixture.getDefaultDepartmentId());
    }

    public static EmployeeCreateRequest createEmployeeCreateRequestWithDefaultDepartment(String email) {
        return createEmployeeCreateRequestWithDepartment(DepartmentFixture.getDefaultDepartmentId(), email);
    }

    // UpdateRequest 메서드들
    public static EmployeeUpdateRequest createEmployeeUpdateRequest() {
        return createEmployeeUpdateRequest("김철수", "updateUser@email.com");
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequest(String name) {
        return createEmployeeUpdateRequest(name, "updateUser@email.com");
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequest(String name, String email) {
        return new EmployeeUpdateRequest(
            UUID.randomUUID(),
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            "Updated memo for the employee."
        );
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequestWithDepartment(UUID departmentId) {
        return createEmployeeUpdateRequestWithDepartment(departmentId, "김철수", "updateUser@email.com");
    }

    public static EmployeeUpdateRequest createEmployeeUpdateRequestWithDepartment(UUID departmentId, String name, String email) {
        return new EmployeeUpdateRequest(
            departmentId,  // 실제 부서 ID 사용
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.DIRECTOR,
            EmployeeType.PART_TIME,
            EmployeeGrade.JUNIOR,
            "Updated memo for the employee."
        );
    }

}