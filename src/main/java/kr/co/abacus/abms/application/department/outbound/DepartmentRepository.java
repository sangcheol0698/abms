package kr.co.abacus.abms.application.department.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentRepository extends Repository<Department, UUID>, CustomDepartmentRepository {

    Department save(Department department);

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    List<Department> findAllByDeletedFalse();

    boolean existsByIdAndDeletedFalse(UUID newDepartmentId);

    void saveAll(Iterable<Department> departments);

}