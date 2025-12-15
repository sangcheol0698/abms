package kr.co.abacus.abms.application.department.inbound;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(UUID id);

    List<OrganizationChartInfo> getOrganizationChart();

    Page<EmployeeSummary> getEmployees(UUID departmentId, String name, Pageable pageable);

}
