package kr.co.abacus.abms.domain.department;

import java.util.UUID;

public class DepartmentFixture {

    // 기본 테스트용 부서들
    public static Department createRootDepartment() {
        return Department.createRoot(createRootDepartmentCreateRequest());
    }

    public static Department createSubDepartment() {
        Department parent = createRootDepartment();
        return Department.create(createSubDepartmentCreateRequest(), parent);
    }

    public static Department createTestCompany() {
        return Department.createRoot(createDepartmentCreateRequest("테스트회사", "TEST_COMPANY", DepartmentType.COMPANY));
    }

    public static Department createTestDivision() {
        Department company = createTestCompany();
        return Department.create(createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION), company);
    }

    public static Department createTestTeam() {
        Department division = createTestDivision();
        return Department.create(createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM), division);
    }

    // Request 생성 메서드들
    public static DepartmentCreateRequest createRootDepartmentCreateRequest() {
        return createDepartmentCreateRequest("(주)애버커스", "ABACUS", DepartmentType.COMPANY);
    }

    public static DepartmentCreateRequest createSubDepartmentCreateRequest() {
        return createDepartmentCreateRequest("연구개발본부", "RND", DepartmentType.DIVISION);
    }

    public static DepartmentCreateRequest createDepartmentCreateRequest(String name, String code, DepartmentType type) {
        return new DepartmentCreateRequest(null, code, name, type, null);
    }

    public static DepartmentCreateRequest createDepartmentCreateRequest(String name, String code) {
        return createDepartmentCreateRequest(name, code, DepartmentType.TEAM);
    }

    // 부서 ID 헬퍼 메서드들 - 주의: 통합 테스트에서는 IntegrationTestBase의 메서드를 사용하세요
    public static UUID getDefaultDepartmentId() {
        return createTestCompany().getId();
    }

    public static UUID getTestDivisionId() {
        return createTestDivision().getId();
    }

    public static UUID getTestTeamId() {
        return createTestTeam().getId();
    }

}