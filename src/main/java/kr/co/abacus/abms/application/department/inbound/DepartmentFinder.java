package kr.co.abacus.abms.application.department.inbound;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.domain.department.Department;

public interface DepartmentFinder {

    Department find(Long id);

    List<OrganizationChartDetail> getOrganizationChart();

    Page<EmployeeSummary> getEmployees(Long departmentId, String name, Pageable pageable);

    DepartmentDetail findDetail(Long departmentId);

}
