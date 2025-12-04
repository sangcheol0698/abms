package kr.co.abacus.abms.application.department.required;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentRepository extends Repository<Department, UUID>, CustomDepartmentRepository {

    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.children WHERE d.deleted = false")
    List<Department> findAllByDeletedFalseWithChildren();

    List<Department> findAllByDeletedFalse();

    List<Department> findAllByIdInAndDeletedFalse(List<UUID> ids);

}