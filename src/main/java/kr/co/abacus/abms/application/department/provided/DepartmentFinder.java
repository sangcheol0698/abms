package kr.co.abacus.abms.application.department.provided;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

public interface DepartmentFinder {

    Department find(UUID id);

    List<Department> findAll();

    OrganizationChartModel getOrganizationChart();

    OrganizationChartWithEmployeesModel getOrganizationChartWithEmployees();

    Page<Employee> getEmployees(UUID departmentId, String name, Pageable pageable);

}
