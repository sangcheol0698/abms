package kr.co.abacus.abms.application.department.required;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class DepartmentRepositoryTest extends IntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
    }

    @Test
    void findAllByDeletedFalse() {
        List<Department> departmentsFindAll = departmentRepository.findAllByDeletedFalse();
        Department department = departmentsFindAll.getFirst();
        department.softDelete("testAdmin");

        List<Department> departmentsByDeletedFalse = departmentRepository.findAllByDeletedFalse();
        assertThat(departmentsByDeletedFalse).hasSize(departmentsFindAll.size() - 1);
    }

}