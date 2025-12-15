package kr.co.abacus.abms.application.department.outbound;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;
import kr.co.abacus.abms.domain.department.Department;

public interface CustomDepartmentRepository {

    Map<UUID, Long> getDepartmentHeadcounts();

    Optional<Department> findByName(String name);

    List<OrganizationChartInfo> getOrganizationChart();

    List<DepartmentProjection> findAllDepartmentProjections();

}
