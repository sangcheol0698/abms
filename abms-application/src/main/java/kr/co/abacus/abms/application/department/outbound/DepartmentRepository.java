package kr.co.abacus.abms.application.department.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentRepository extends CustomDepartmentRepository {

    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(Long id);

    List<Department> findAllByDeletedFalse();

    boolean existsByIdAndDeletedFalse(Long newDepartmentId);

    void saveAll(Iterable<Department> departments);

}
