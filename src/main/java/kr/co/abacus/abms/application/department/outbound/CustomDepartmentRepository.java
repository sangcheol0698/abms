package kr.co.abacus.abms.application.department.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.domain.department.Department;

public interface CustomDepartmentRepository {

    Optional<Department> findByName(String name);

    List<DepartmentProjection> findAllDepartmentProjections();

    Optional<DepartmentDetail> findDetail(Long departmentId);

}
