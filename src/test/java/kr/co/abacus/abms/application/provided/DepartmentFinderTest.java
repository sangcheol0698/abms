package kr.co.abacus.abms.application.provided;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;

@Transactional
@SpringBootTest
class DepartmentFinderTest {

    @Autowired
    private DepartmentFinder departmentFinder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void find() {
        Department savedDepartment = DepartmentFixture.createRootDepartment();
        departmentRepository.save(savedDepartment);
        entityManager.flush();
        entityManager.clear();

        Department foundDepartment = departmentFinder.find(savedDepartment.getId());
        assertThat(foundDepartment.getId()).isEqualTo(savedDepartment.getId());
        assertThat(foundDepartment.getName()).isEqualTo(savedDepartment.getName());
        assertThat(foundDepartment.getCode()).isEqualTo(savedDepartment.getCode());
        assertThat(foundDepartment.getType()).isEqualTo(savedDepartment.getType());
    }

    @Test
    void findNotFound() {
        assertThatThrownBy(() -> departmentFinder.find(UUID.randomUUID()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findDeleted() {
        Department savedDepartment = DepartmentFixture.createRootDepartment();
        departmentRepository.save(savedDepartment);
        entityManager.flush();

        savedDepartment.softDelete("testUser");
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> departmentFinder.find(savedDepartment.getId()))
            .isInstanceOf(IllegalArgumentException.class);
    }

}