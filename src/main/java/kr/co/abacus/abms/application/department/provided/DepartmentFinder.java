package kr.co.abacus.abms.application.department.provided;

import java.util.UUID;

import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(UUID id);

    OrganizationChartModel getOrganizationChart();

}
