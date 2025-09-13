package kr.co.abacus.abms.application.department.required;

import kr.co.abacus.abms.domain.department.Department;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends Repository<Department, UUID> {

    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    Optional<Department> findByCodeAndDeletedFalse(String code);

    List<Department> findAll();

    boolean existsById(UUID departmentId);
}