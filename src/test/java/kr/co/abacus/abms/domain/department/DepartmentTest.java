package kr.co.abacus.abms.domain.department;

import static kr.co.abacus.abms.domain.department.DepartmentFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DepartmentTest {

    @Test
    void createRoot() {
        Department rootDepartment = createRootDepartment();

        assertThat(rootDepartment.getName()).isEqualTo("(주)애버커스");
        assertThat(rootDepartment.getCode()).isEqualTo("ABACUS");
        assertThat(rootDepartment.getType()).isEqualTo(DepartmentType.COMPANY);
        assertThat(rootDepartment.getParent()).isNull();
        assertThat(rootDepartment.getChildren()).isEmpty();
    }

    @Test
    void createSubDepartment() {
        Department parent = createRootDepartment();
        DepartmentCreateRequest request = createSubDepartmentCreateRequest();

        Department subDepartment = Department.create(request, parent);

        assertThat(subDepartment.getName()).isEqualTo("연구개발본부");
        assertThat(subDepartment.getCode()).isEqualTo("RND");
        assertThat(subDepartment.getType()).isEqualTo(DepartmentType.DIVISION);
        assertThat(subDepartment.getParent()).isEqualTo(parent);
        assertThat(parent.getChildren()).contains(subDepartment);
    }

    @Test
    void createSubDepartmentWithNullParent() {
        DepartmentCreateRequest request = createSubDepartmentCreateRequest();

        assertThatThrownBy(() -> Department.create(request, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("상위 부서는 필수입니다");
    }

    @Test
    void hierarchicalRelationship() {
        Department company = createRootDepartment();
        Department division = Department.create(
            createDepartmentCreateRequest("연구개발본부", "RND", DepartmentType.DIVISION, null),
            company
        );
        Department team = Department.create(
            createDepartmentCreateRequest("플랫폼팀", "PLATFORM", DepartmentType.TEAM, null),
            division
        );

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

        Department child1 = Department.create(
            createDepartmentCreateRequest("연구개발본부", "RND", DepartmentType.DIVISION, null),
            parent
        );
        Department child2 = Department.create(
            createDepartmentCreateRequest("경영기획본부", "PLANNING", DepartmentType.DIVISION, null),
            parent
        );
        Department child3 = Department.create(
            createDepartmentCreateRequest("영업본부", "SALES", DepartmentType.DIVISION, null),
            parent
        );

        assertThat(parent.getChildren()).hasSize(3);
        assertThat(parent.getChildren()).containsExactlyInAnyOrder(child1, child2, child3);
    }

}