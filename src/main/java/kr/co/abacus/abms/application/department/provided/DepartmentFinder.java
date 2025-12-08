package kr.co.abacus.abms.application.department.provided;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(UUID id);

    List<Department> findAll();

    List<Department> findAllByIds(List<UUID> ids);

    OrganizationChartModel getOrganizationChart();

    OrganizationChartWithEmployeesModel getOrganizationChartWithEmployees();

    Page<EmployeeResponse> getEmployees(UUID departmentId, String name, Pageable pageable);

}
