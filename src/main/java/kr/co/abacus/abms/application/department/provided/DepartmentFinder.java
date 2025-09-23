package kr.co.abacus.abms.application.department.provided;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(UUID id);

    List<Department> findAll();

    OrganizationChartModel getOrganizationChart();

    OrganizationChartWithEmployeesModel getOrganizationChartWithEmployees();

}
