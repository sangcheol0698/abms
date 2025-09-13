package kr.co.abacus.abms.application.department.provided;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.department.DepartmentType;
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

    @Test
    void findDeleted() {
        // 새로운 부서를 만들어서 삭제 테스트 - 고유한 코드 사용
        Department newDepartment = Department.createRoot(
            DepartmentFixture.createDepartmentCreateRequest("삭제테스트회사", "DELETE_TEST_COMPANY", DepartmentType.COMPANY)
        );
        departmentRepository.save(newDepartment);
        flush();

        newDepartment.softDelete("testUser");
        flushAndClear();

        assertThatThrownBy(() -> departmentFinder.find(newDepartment.getId()))
            .isInstanceOf(DepartmentNotFoundException.class);
    }

}