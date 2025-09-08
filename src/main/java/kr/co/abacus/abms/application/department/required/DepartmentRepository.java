package kr.co.abacus.abms.application.department.required;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentRepository extends Repository<Department, UUID> {

    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    Optional<Department> findByCodeAndDeletedFalse(String code);

    List<Department> findAll();

}