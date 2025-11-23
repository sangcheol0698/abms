package kr.co.abacus.abms.application.department.required;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;

public interface CustomDepartmentRepository {

    Map<UUID, Long> getDepartmentHeadcounts();

    Optional<Department> findByName(String name);
}
