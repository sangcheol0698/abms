package kr.co.abacus.abms.domain.department;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DepartmentTest {

    @Test
    void createRoot() {
        Department rootDepartment = createRootDepartment();

        assertThat(rootDepartment.getName()).isEqualTo("테스트회사");
        assertThat(rootDepartment.getCode()).isEqualTo("CODE-TEST");
        assertThat(rootDepartment.getType()).isEqualTo(DepartmentType.COMPANY);
        assertThat(rootDepartment.getParent()).isNull();
        assertThat(rootDepartment.getChildren()).isEmpty();
    }

    @Test
    void createSub() {
        Department parent = createRootDepartment();
        Department subDepartment = createSubDepartment(parent);

        assertThat(subDepartment.getName()).isEqualTo("테스트부서");
        assertThat(subDepartment.getCode()).isEqualTo("CODE-SUBTEST");
        assertThat(subDepartment.getType()).isEqualTo(DepartmentType.DIVISION);
        assertThat(subDepartment.getParent()).isEqualTo(parent);
        assertThat(parent.getChildren()).contains(subDepartment);
    }

    @Test
    @DisplayName("부서 간 계층 관계 설정 및 조회")
    void hierarchicalRelationship() {
        Department company = createRootDepartment();
        Department division = createSubDepartment(company);
        Department team = createSubDepartment(division);

        // 계층 구조 검증
        assertThat(company.getParent()).isNull();
        assertThat(company.getChildren()).hasSize(1);
        assertThat(company.getChildren()).contains(division);

        assertThat(division.getParent()).isEqualTo(company);
        assertThat(division.getChildren()).hasSize(1);
        assertThat(division.getChildren()).contains(team);

        assertThat(team.getParent()).isEqualTo(division);
        assertThat(team.getChildren()).isEmpty();
    }

    @Test
    void multipleChildDepartments() {
        Department parent = createRootDepartment();

        Department child1 = createSubDepartment(parent);
        Department child2 = createSubDepartment(parent);
        Department child3 = createSubDepartment(parent);

        assertThat(parent.getChildren()).hasSize(3);
        assertThat(parent.getChildren()).containsExactlyInAnyOrder(child1, child2, child3);
    }

    private Department createRootDepartment() {
        return Department.create(
            "CODE-TEST",
            "테스트회사",
            DepartmentType.COMPANY,
            null,
            null
        );
    }

    private Department createSubDepartment(Department parent) {
        return Department.create(
            "CODE-SUBTEST",
            "테스트부서",
            DepartmentType.DIVISION,
            null,
            parent
        );
    }

}