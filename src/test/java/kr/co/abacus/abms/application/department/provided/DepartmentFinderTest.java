package kr.co.abacus.abms.application.department.provided;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.support.IntegrationTestBase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepartmentFinderTest extends IntegrationTestBase {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Test
    void find() {
        // 베이스 클래스에서 이미 testCompany가 생성되어 있음
        Department foundDepartment = departmentFinder.find(getDefaultDepartmentId());

        assertThat(foundDepartment.getId()).isEqualTo(getDefaultDepartmentId());
        assertThat(foundDepartment.getName()).isEqualTo("테스트회사");
        assertThat(foundDepartment.getCode()).isEqualTo("TEST_COMPANY");
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> departmentFinder.find(UUID.randomUUID()))
            .isInstanceOf(DepartmentNotFoundException.class);
    }

}