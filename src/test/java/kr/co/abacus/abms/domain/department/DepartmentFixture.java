package kr.co.abacus.abms.domain.department;

public class DepartmentFixture {

    public static Department createRootDepartment() {
        return Department.createRoot(createRootDepartmentCreateRequest());
    }

    public static Department createSubDepartment() {
        Department parent = createRootDepartment();
        return Department.create(createSubDepartmentCreateRequest(), parent);
    }

    public static DepartmentCreateRequest createRootDepartmentCreateRequest() {
        return createDepartmentCreateRequest("(주)애버커스", "ABACUS", DepartmentType.COMPANY);
    }

    public static DepartmentCreateRequest createSubDepartmentCreateRequest() {
        return createDepartmentCreateRequest("연구개발본부", "RND", DepartmentType.DIVISION);
    }

    public static DepartmentCreateRequest createDepartmentCreateRequest(String name, String code, DepartmentType type) {
        return new DepartmentCreateRequest(
                null,
                code,
                name,
                type
        );
    }

    public static DepartmentCreateRequest createDepartmentCreateRequest(String name, String code) {
        return createDepartmentCreateRequest(name, code, DepartmentType.TEAM);
    }

}